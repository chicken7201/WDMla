package com.gtnewhorizons.wdmla.overlay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.junit.jupiter.api.Test;

class SnowCoverRenderBlocksTest {

    private static final int X = -7;
    private static final int Y = 64;
    private static final int Z = -4;
    private static final int GRASS_TINT = 0x679C42;
    private static final IIcon GRASS = icon("mod:grass_top", 0);
    private static final IIcon SNOW = icon("mod:snow_variant", 16);
    private static final IIcon OVERRIDE = icon("mod:breaking", 32);

    /** Checks emitted vertices for inherited grass, independent mod grass, and other covered surfaces. */
    @Test
    void snowCoversDifferentBlockImplementations() throws Exception {
        Block[] surfaces = { new BlockGrass() {}, new Block(Material.grass) {}, new Block(Material.ground) {},
                new Block(Material.rock) {} };
        for (Block surface : surfaces) {
            SnowCoverRenderBlocks renderer = renderer(snow(Material.snow, 0xFFFFFF));
            assertFace(capture(renderer, surface, false), SNOW, 0xFFFFFF, Y + 1);
        }
    }

    /** Checks that full snow blocks supply their world-state texture and tint at negative coordinates. */
    @Test
    void snowUsesCoverMetadataAndTint() throws Exception {
        SnowCoverRenderBlocks renderer = renderer(snow(Material.craftedSnow, 0x85AADD));
        assertFace(capture(renderer, new Block(Material.grass) {}, false), SNOW, 0x85AADD, Y + 1);
    }

    /** Checks that air or an unrelated block above leaves the original surface unchanged. */
    @Test
    void uncoveredSurfacesKeepTheirTextureAndTint() throws Exception {
        for (Material material : new Material[] { Material.air, Material.rock }) {
            SnowCoverRenderBlocks renderer = renderer(new Block(material) {});
            assertFace(capture(renderer, new Block(Material.grass) {}, false), GRASS, GRASS_TINT, Y + 1);
        }
    }

    /** Checks that snow in the cell above does not recolor a lower slab with a gap beneath the snow. */
    @Test
    void lowerSlabKeepsItsExposedTop() throws Exception {
        SnowCoverRenderBlocks renderer = renderer(snow(Material.snow, 0xFFFFFF));
        renderer.renderMaxY = 0.5;
        assertFace(capture(renderer, new Block(Material.ground) {}, false), GRASS, GRASS_TINT, Y + 0.5f);
    }

    /** Checks that upper grass slabs touching the snow are covered even when their material is ground. */
    @Test
    void upperSlabReceivesSnowCover() throws Exception {
        SnowCoverRenderBlocks renderer = renderer(snow(Material.snow, 0xFFFFFF));
        renderer.renderMinY = 0.5;
        assertFace(capture(renderer, new Block(Material.ground) {}, false), SNOW, 0xFFFFFF, Y + 1);
    }

    /** Checks that a renderer's explicit replacement texture and color take precedence over snow. */
    @Test
    void explicitTextureOverrideIsPreserved() throws Exception {
        SnowCoverRenderBlocks renderer = renderer(snow(Material.snow, 0xFFFFFF));
        renderer.setOverrideBlockTexture(OVERRIDE);
        assertFace(capture(renderer, new Block(Material.grass) {}, false), OVERRIDE, GRASS_TINT, Y + 1);
    }

    /** Checks that custom AO corner colors cannot tint snow and the renderer state is restored afterward. */
    @Test
    void snowTintDoesNotLeakThroughAmbientOcclusion() throws Exception {
        SnowCoverRenderBlocks renderer = renderer(snow(Material.snow, 0xFFFFFF));
        renderer.enableAO = true;
        assertFace(capture(renderer, new Block(Material.grass) {}, false), SNOW, 0xFFFFFF, Y + 1);
        assertTrue(renderer.enableAO);
    }

    /** Checks that snow replacement does not alter the base block's side texture or tint. */
    @Test
    void sideFacesRemainUnchanged() throws Exception {
        SnowCoverRenderBlocks renderer = renderer(snow(Material.snow, 0xFFFFFF));
        int[] vertices = capture(renderer, new Block(Material.grass) {}, true);
        assertTextureAndTint(vertices, GRASS, GRASS_TINT);
    }

    /** Creates separated atlas regions so captured UVs identify the texture actually used. */
    private static IIcon icon(String name, int originX) {
        TextureAtlasSprite sprite = new TextureAtlasSprite(name) {};
        sprite.setIconWidth(16);
        sprite.setIconHeight(16);
        sprite.initSprite(64, 64, originX, 0, false);
        return sprite;
    }

    /** Models a mod snow block whose top texture and color depend on the covering cell's metadata. */
    private static Block snow(Material material, int tint) {
        return new Block(material) {

            /** Returns the snow variant selected by the real cover coordinates and top face. */
            @Override
            public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
                assertEquals(1, side);
                return world.getBlockMetadata(x, y, z) == 3 ? SNOW : GRASS;
            }

            /** Supplies the snow variant's own tint independently of the underlying grass color. */
            @Override
            public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
                return world.getBlockMetadata(x, y, z) == 3 ? tint : GRASS_TINT;
            }
        };
    }

    /** Supplies a full preview surface and a small world fixture with a cover at the expected coordinates. */
    private static SnowCoverRenderBlocks renderer(Block cover) {
        SnowCoverRenderBlocks renderer = new SnowCoverRenderBlocks();
        renderer.renderMaxX = renderer.renderMaxY = renderer.renderMaxZ = 1;
        renderer.blockAccess = (IBlockAccess) Proxy.newProxyInstance(
                IBlockAccess.class.getClassLoader(),
                new Class<?>[] { IBlockAccess.class },
                (proxy, method, args) -> {
                    assertEquals(X, args[0]);
                    assertEquals(Y + 1, args[1]);
                    assertEquals(Z, args[2]);
                    if (method.getName().equals("getBlock")) return cover;
                    if (method.getName().equals("getBlockMetadata")) return 3;
                    throw new AssertionError("Unexpected world access: " + method.getName());
                });
        return renderer;
    }

    /** Captures the real renderer's vertex buffer without submitting a draw to OpenGL. */
    private static int[] capture(SnowCoverRenderBlocks renderer, Block surface, boolean side) throws Exception {
        Tessellator tessellator = Tessellator.instance;
        Field drawing = Tessellator.class.getDeclaredField("isDrawing");
        drawing.setAccessible(true);
        tessellator.startDrawingQuads();
        try {
            tessellator.setColorOpaque_I(GRASS_TINT);
            if (side) {
                renderer.renderFaceZPos(surface, X, Y, Z, GRASS);
            } else {
                renderer.renderFaceYPos(surface, X, Y, Z, GRASS);
            }
            return tessellator.getVertexState(0, 0, 0).getRawBuffer();
        } finally {
            // Release the batch without requiring a graphics context; the next batch resets the buffer.
            drawing.setBoolean(tessellator, false);
        }
    }

    /** Checks the rendered top's height as well as its actual atlas region and vertex colors. */
    private static void assertFace(int[] vertices, IIcon icon, int tint, float height) {
        assertTextureAndTint(vertices, icon, tint);
        for (int i = 0; i < vertices.length; i += 8) {
            assertEquals(height, Float.intBitsToFloat(vertices[i + 1]));
        }
    }

    /** Checks all four UV corners and RGBA bytes of the emitted quad. */
    private static void assertTextureAndTint(int[] vertices, IIcon icon, int tint) {
        assertEquals(32, vertices.length);
        int minUCount = 0;
        int minVCount = 0;
        for (int i = 0; i < vertices.length; i += 8) {
            float u = Float.intBitsToFloat(vertices[i + 3]);
            float v = Float.intBitsToFloat(vertices[i + 4]);
            assertTrue(u == icon.getMinU() || u == icon.getMaxU());
            assertTrue(v == icon.getMinV() || v == icon.getMaxV());
            if (u == icon.getMinU()) minUCount++;
            if (v == icon.getMinV()) minVCount++;
            byte[] rgba = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(vertices[i + 5]).array();
            assertEquals(tint >> 16 & 255, rgba[0] & 255);
            assertEquals(tint >> 8 & 255, rgba[1] & 255);
            assertEquals(tint & 255, rgba[2] & 255);
            assertEquals(255, rgba[3] & 255);
        }
        assertEquals(2, minUCount);
        assertEquals(2, minVCount);
    }
}

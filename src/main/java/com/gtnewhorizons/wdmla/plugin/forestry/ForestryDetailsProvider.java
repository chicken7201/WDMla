package com.gtnewhorizons.wdmla.plugin.forestry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import com.google.common.collect.Lists;
import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;
import com.mojang.authlib.GameProfile;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.arboriculture.EnumTreeChromosome;
import forestry.api.arboriculture.ITree;
import forestry.api.arboriculture.ITreeGenome;
import forestry.api.core.ForestryAPI;
import forestry.api.core.IErrorLogic;
import forestry.api.core.IErrorState;
import forestry.api.genetics.IGenome;
import forestry.apiculture.BeekeepingLogic;
import forestry.apiculture.genetics.Bee;
import forestry.arboriculture.genetics.Tree;
import forestry.arboriculture.tiles.TileLeaves;
import forestry.arboriculture.tiles.TileTreeContainer;
import forestry.core.access.IOwnable;
import forestry.core.tiles.TileEngine;
import forestry.plugins.PluginApiculture;
import mcp.mobius.waila.Waila;
import mcp.mobius.waila.api.SpecialChars;

/** Native WDMla version of WAILAPlugins' complete Forestry and MagicBees integration. */
public enum ForestryDetailsProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    INSTANCE;

    private static Field throttleField;

    /** Displays engine, tree, leaf, bee inventory, production, error, and breeding information. */
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        if (tile == null) {
            return;
        }
        NBTTagCompound data = accessor.getServerData();
        if (data.hasKey("ForestryEnergy")) {
            tooltip.progress(
                    data.getInteger("ForestryEnergy"),
                    data.getInteger("ForestryMaxEnergy"),
                    StatCollector.translateToLocal("hud.msg.wdmla.energy"));
        }
        if (data.hasKey("ForestryHeat")) {
            tooltip.child(
                    value("hud.msg.wdmla.forestry.engine.heat", data.getInteger("ForestryHeat") / 10D + "°C"));
        }
        if (data.hasKey("ForestryTree") && tile instanceof TileTreeContainer treeContainer) {
            appendTree(tooltip, accessor, data, treeContainer);
        }
        if (tile instanceof TileLeaves && data.hasKey("ForestryPollinated")) {
            tooltip.child(value("hud.msg.wdmla.forestry.pollinated", data.getString("ForestryPollinated")));
        }
        if (tile instanceof IBeeHousing) {
            appendBeeHousing(tooltip, accessor, data);
        }
    }

    /** Displays owner-gated tree genome details, using Show Details for the expanded tooltip. */
    private static void appendTree(
            ITooltip tooltip, BlockAccessor accessor, NBTTagCompound data, TileTreeContainer treeContainer) {
        ITree tree = new Tree(data.getCompoundTag("ForestryTree"));
        String ownerText = data.getString("ForestryOwner");
        UUID owner = ownerText.isEmpty() ? null : UUID.fromString(ownerText);
        boolean allowed = owner != null && owner.equals(accessor.getPlayer().getGameProfile().getId())
                && (tree.isAnalyzed() || treeContainer instanceof TileLeaves);
        if (!allowed) {
            tooltip.child(
                    ThemeHelper.INSTANCE.info(
                            StatCollector.translateToLocal(
                                    tree.isAnalyzed() ? "hud.msg.wdmla.forestry.not.owner"
                                            : "hud.msg.wdmla.forestry.not.analyzed")));
            return;
        }
        if (accessor.showDetails()) {
            List<String> lines = new ArrayList<>();
            tree.addTooltip(lines);
            lines.forEach(tooltip::text);
        } else {
            tooltip.child(ThemeHelper.INSTANCE.info(StatCollector.translateToLocal("hud.msg.wdmla.show.details")));
        }
    }

    /** Displays queen/drone genetics, production/yields, errors, breeding progress, and jubilance. */
    private static void appendBeeHousing(ITooltip tooltip, BlockAccessor accessor, NBTTagCompound data) {
        ItemStack queenStack = loadStack(data, "ForestryQueen");
        ItemStack droneStack = loadStack(data, "ForestryDrone");
        IBee queen = queenStack == null ? null : new Bee(queenStack.getTagCompound());
        if (queen != null) {
            tooltip.child(
                    value(
                            "hud.msg.wdmla.forestry.main.bee",
                            getBeeTypeName(queenStack) + ": " + getSpeciesName(queen.getGenome())));
            appendAnalyzedBee(tooltip, accessor, queen);
        }

        IBee drone = queen != null && queen.getMate() != null ? new Bee(queen.getMate())
                : droneStack == null ? null : new Bee(droneStack.getTagCompound());
        if (drone != null) {
            tooltip.child(
                    value(
                            "hud.msg.wdmla.forestry.secondary.bee",
                            StatCollector.translateToLocal("hud.msg.wdmla.forestry.drone") + ": "
                                    + getSpeciesName(drone.getGenome())));
            appendAnalyzedBee(tooltip, accessor, drone);
        }

        if (queen != null && data.hasKey("ForestryProduction")) {
            appendBeeYield(tooltip, accessor, queen.getGenome(), data.getFloat("ForestryProduction"));
        }
        int[] errorIds = data.getIntArray("ForestryErrors");
        for (int id : errorIds) {
            IErrorState error = ForestryAPI.errorStateRegistry.getErrorState((short) id);
            if (error != null) {
                tooltip.child(
                        ThemeHelper.INSTANCE.failure(
                                StatCollector.translateToLocal(error.getDescription())));
            }
        }
        if (errorIds.length == 0 && data.hasKey("ForestryBreedProgress")) {
            tooltip.progress(
                    Math.round(data.getDouble("ForestryBreedProgress") * 10000),
                    10000,
                    StatCollector.translateToLocal("hud.msg.wdmla.forestry.breed.progress"));
        }
        if (data.hasKey("ForestryJubilant")) {
            tooltip.child(
                    (data.getBoolean("ForestryJubilant") ? ThemeHelper.INSTANCE.success(
                            StatCollector.translateToLocal("hud.msg.wdmla.forestry.jubilant"))
                            : ThemeHelper.INSTANCE.failure(
                                    StatCollector.translateToLocal("hud.msg.wdmla.forestry.not.jubilant"))));
        }
    }

    /** Displays an analyzed bee's full genome only while Show Details is held. */
    private static void appendAnalyzedBee(ITooltip tooltip, BlockAccessor accessor, IBee bee) {
        if (!bee.isAnalyzed()) {
            return;
        }
        if (!accessor.showDetails()) {
            tooltip.child(ThemeHelper.INSTANCE.info(StatCollector.translateToLocal("hud.msg.wdmla.show.details")));
            return;
        }
        List<String> lines = new ArrayList<>();
        bee.addTooltip(lines);
        for (String line : lines) {
            tooltip.text(SpecialChars.TAB + line);
        }
    }

    /** Displays the effective production multiplier and exact average output yields per hour. */
    private static void appendBeeYield(
            ITooltip tooltip, BlockAccessor accessor, IBeeGenome genome, float productionModifier) {
        float speed = genome.getSpeed();
        float dummyProduction = 100F * Math.min(Bee.getFinalChance(0.01F, speed, productionModifier, 1F), 1F);
        tooltip.child(
                value(
                        "hud.msg.wdmla.forestry.production",
                        String.format("b^0.52 * %.3f", dummyProduction)));
        if (!accessor.showDetails()) {
            tooltip.child(ThemeHelper.INSTANCE.info(StatCollector.translateToLocal("hud.msg.wdmla.show.details")));
            return;
        }

        Map<ItemStack, Float> yields = new HashMap<>();
        extendYields(yields, genome.getPrimary().getProductChances(), false, speed, productionModifier);
        extendYields(yields, genome.getSecondary().getProductChances(), true, speed, productionModifier);
        extendYields(yields, genome.getPrimary().getSpecialtyChances(), false, speed, productionModifier);
        TreeMap<Float, List<ItemStack>> grouped = new TreeMap<>();
        for (Map.Entry<ItemStack, Float> entry : yields.entrySet()) {
            grouped.computeIfAbsent(entry.getValue(), ignored -> new ArrayList<>()).add(entry.getKey());
        }
        for (Map.Entry<Float, List<ItemStack>> entry : grouped.descendingMap().entrySet()) {
            ItemStack item = entry.getValue().get(0);
            String duplicate = entry.getValue().size() > 1 ? ", ..." : "";
            tooltip.text(
                    SpecialChars.TAB + item.getDisplayName()
                            + duplicate
                            + ": "
                            + String.format("%.3f/h", entry.getKey() * 60 * 60 / 27.5));
        }
    }

    /** Merges primary, secondary, and specialty product chances using Forestry's final-chance formula. */
    private static void extendYields(
            Map<ItemStack, Float> yields,
            Map<ItemStack, Float> products,
            boolean secondary,
            float speed,
            float productionModifier) {
        for (Map.Entry<ItemStack, Float> product : products.entrySet()) {
            float chance = product.getValue() / (secondary ? 2F : 1F);
            float finalChance = Math.min(Bee.getFinalChance(chance, speed, productionModifier, 1F), 1F);
            ItemStack existing = findEquivalentStack(yields, product.getKey());
            if (existing == null) {
                yields.put(product.getKey(), finalChance);
            } else {
                yields.put(existing, yields.get(existing) + finalChance);
            }
        }
    }

    /** Finds an item-and-metadata equivalent stack in a map whose ItemStack keys use identity equality. */
    private static ItemStack findEquivalentStack(Map<ItemStack, Float> values, ItemStack target) {
        for (ItemStack candidate : values.keySet()) {
            if (ItemStack.areItemStacksEqual(candidate, target)) {
                return candidate;
            }
        }
        return null;
    }

    /** Returns the active species name from a bee genome. */
    private static String getSpeciesName(IGenome genome) {
        return genome.getActiveAllele(EnumBeeChromosome.SPECIES).getName();
    }

    /** Resolves Forestry's localized bee cast name from the serialized bee stack. */
    private static String getBeeTypeName(ItemStack bee) {
        if (bee.getItem() == PluginApiculture.items.beeDroneGE) {
            return StatCollector.translateToLocal("hud.msg.wdmla.forestry.drone");
        }
        if (bee.getItem() == PluginApiculture.items.beePrincessGE) {
            return StatCollector.translateToLocal("hud.msg.wdmla.forestry.princess");
        }
        return StatCollector.translateToLocal("hud.msg.wdmla.forestry.queen");
    }

    /** Loads an optional ItemStack stored under the requested compound key. */
    private static ItemStack loadStack(NBTTagCompound data, String key) {
        return data.hasKey(key, 10) ? ItemStack.loadItemStackFromNBT(data.getCompoundTag(key)) : null;
    }

    /** Collects all Forestry machine, tree, leaf, and bee data used on the client. */
    @Override
    public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        if (tile instanceof TileLeaves leaves) {
            ITreeGenome mate = leaves.getTree().getMate();
            if (mate != null) {
                data.setString(
                        "ForestryPollinated",
                        mate.getActiveAllele(EnumTreeChromosome.SPECIES).getName());
            }
        }
        if (tile instanceof IBeeHousing housing) {
            appendBeeHousingData(data, housing);
        }
        if (tile instanceof TileTreeContainer treeContainer && treeContainer.getTree() != null) {
            NBTTagCompound tree = new NBTTagCompound();
            treeContainer.getTree().writeToNBT(tree);
            data.setTag("ForestryTree", tree);
        }
        if (tile instanceof TileEngine) {
            appendEngineData(data, tile);
        }
        if (tile instanceof IOwnable ownable) {
            GameProfile owner = ownable.getOwner();
            if (owner != null && owner.getId() != null) {
                data.setString("ForestryOwner", owner.getId().toString());
            }
        }
    }

    /** Reads Forestry engine energy and heat without linking optional CoFH and BuildCraft interfaces. */
    private static void appendEngineData(NBTTagCompound data, Object engine) {
        try {
            Object manager = engine.getClass().getMethod("getEnergyManager").invoke(engine);
            int stored = ((Number) manager.getClass().getMethod("getTotalEnergyStored").invoke(manager)).intValue();
            int maximum = ((Number) manager.getClass().getMethod("getMaxEnergyStored").invoke(manager)).intValue();
            int heat = ((Number) engine.getClass().getMethod("getHeat").invoke(engine)).intValue();
            data.setInteger("ForestryEnergy", stored);
            data.setInteger("ForestryMaxEnergy", maximum);
            data.setInteger("ForestryHeat", heat);
        } catch (ReflectiveOperationException e) {
            Waila.log.warn("Unable to read Forestry engine energy and heat", e);
        }
    }

    /** Serializes bee stacks, error states, breeding interpolation, production, and jubilance. */
    private static void appendBeeHousingData(NBTTagCompound data, IBeeHousing housing) {
        IBeekeepingLogic logic = housing.getBeekeepingLogic();
        IBeeHousingInventory inventory = housing.getBeeInventory();
        IErrorLogic errors = housing.getErrorLogic();
        if (logic == null || inventory == null) {
            return;
        }
        ItemStack queen = inventory.getQueen();
        ItemStack drone = inventory.getDrone();
        writeStack(data, "ForestryQueen", queen);
        writeStack(data, "ForestryDrone", drone);
        if (errors != null) {
            List<Integer> ids = Lists.newArrayList();
            for (IErrorState error : errors.getErrorStates()) {
                ids.add((int) error.getID());
            }
            int[] primitive = new int[ids.size()];
            for (int index = 0; index < ids.size(); index++) {
                primitive[index] = ids.get(index);
            }
            data.setIntArray("ForestryErrors", primitive);
        }
        if (queen == null || queen.getItem() != PluginApiculture.items.beeQueenGE) {
            return;
        }
        Bee queenBee = new Bee(queen.getTagCompound());
        try {
            float throttle = getThrottleField().getInt(logic);
            float maximumAge = queenBee.getMaxHealth();
            float age = Math.abs(queenBee.getHealth() - maximumAge);
            float progress = (1F / maximumAge) * (throttle / PluginApiculture.ticksPerBeeWorkCycle);
            data.setDouble("ForestryBreedProgress", age / maximumAge + progress);
        } catch (ReflectiveOperationException e) {
            Waila.log.warn("Unable to interpolate Forestry bee breeding progress", e);
        }

        IBeeGenome genome = queenBee.getGenome();
        float production = BeeManager.beeRoot.createBeeHousingModifier(housing)
                .getProductionModifier(genome, 0F);
        production += BeeManager.beeRoot.getBeekeepingMode(housing.getWorld()).getBeeModifier()
                .getProductionModifier(genome, production);
        data.setFloat("ForestryProduction", production);
        data.setBoolean(
                "ForestryJubilant",
                genome.getPrimary().isJubilant(genome, housing)
                        && genome.getSecondary().isJubilant(genome, housing));
    }

    /** Resolves Forestry's renamed beekeeping throttle field across supported GTNH versions. */
    private static Field getThrottleField() throws NoSuchFieldException {
        if (throttleField == null) {
            try {
                throttleField = BeekeepingLogic.class.getDeclaredField("throttle");
            } catch (NoSuchFieldException ignored) {
                throttleField = BeekeepingLogic.class.getDeclaredField("queenWorkCycleThrottle");
            }
            throttleField.setAccessible(true);
        }
        return throttleField;
    }

    /** Writes an optional ItemStack into one compact compound. */
    private static void writeStack(NBTTagCompound data, String key, ItemStack stack) {
        if (stack != null) {
            NBTTagCompound serialized = new NBTTagCompound();
            stack.writeToNBT(serialized);
            data.setTag(key, serialized);
        }
    }

    /** Builds a themed localized key/value row. */
    private static com.gtnewhorizons.wdmla.api.ui.IComponent value(String key, String value) {
        return ThemeHelper.INSTANCE.value(StatCollector.translateToLocal(key), value);
    }

    /** Returns the stable provider identifier. */
    @Override
    public ResourceLocation getUid() {
        return ForestryPlugin.path("wailaplugins_details");
    }
}

package com.gtnewhorizons.wdmla.plugin.ae2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.IComponent;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;
import com.gtnewhorizons.wdmla.impl.ui.component.HPanelComponent;
import com.gtnewhorizons.wdmla.util.FormatUtil;

import mcp.mobius.waila.utils.WailaExceptionHandler;

/** Summarizes only the cells installed in one ME Chest or ME Drive. */
public final class AE2StorageSummaryProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    private static final ResourceLocation UID = new ResourceLocation("ae2", "storage_summary");
    private static final String CHEST_OR_DRIVE = "appeng.api.implementations.tiles.IChestOrDrive";

    private final Class<?> chestOrDriveClass;
    private final Method getCellCount;
    private final Method getCellStatus;
    private final Method getCellType;
    @Nullable
    private final ByteApi byteApi;

    /** Resolves AE2's public local-cell API once during optional plugin loading. */
    private AE2StorageSummaryProvider() throws ReflectiveOperationException {
        chestOrDriveClass = Class.forName(CHEST_OR_DRIVE);
        getCellCount = chestOrDriveClass.getMethod("getCellCount");
        getCellStatus = chestOrDriveClass.getMethod("getCellStatus", int.class);
        getCellType = chestOrDriveClass.getMethod("getCellType", int.class);
        byteApi = ByteApi.create(chestOrDriveClass);
    }

    /** Creates the summary provider only when the installed AE2 exposes the expected public API. */
    @Nullable
    public static AE2StorageSummaryProvider create() {
        try {
            return new AE2StorageSummaryProvider();
        } catch (ReflectiveOperationException exception) {
            WailaExceptionHandler.handleErr(exception, AE2StorageSummaryProvider.class.getName(), null);
            return null;
        }
    }

    /** Renders installed-cell, fill-state, and storage-type counts from synchronized server data. */
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        NBTTagCompound data = accessor.getServerData();
        int installed = data.getInteger("AE2Installed");
        int slots = data.getInteger("AE2Slots");
        tooltip.child(value("hud.msg.wdmla.ae2.cells", installed + " / " + slots));
        if (installed <= 0) {
            return;
        }

        String usage = StatCollector.translateToLocalFormatted(
                "hud.msg.wdmla.ae2.cell.usage.value",
                data.getInteger("AE2Empty"),
                data.getInteger("AE2Used"),
                data.getInteger("AE2Full"));
        tooltip.child(value("hud.msg.wdmla.ae2.cell.usage", usage));

        long totalBytes = data.getLong("AE2TotalBytes");
        if (totalBytes > 0L) {
            String bytes = FormatUtil.STANDARD.format(data.getLong("AE2UsedBytes")) + " / "
                    + FormatUtil.STANDARD.format(totalBytes);
            tooltip.child(value("hud.msg.wdmla.ae2.cell.bytes", bytes));
        }

        String types = StatCollector.translateToLocalFormatted(
                "hud.msg.wdmla.ae2.cell.types.value",
                data.getInteger("AE2Items"),
                data.getInteger("AE2Fluids"),
                data.getInteger("AE2Essentia"));
        tooltip.child(value("hud.msg.wdmla.ae2.cell.types", types));
    }

    /** Reads at most ten local cell status entries without traversing the attached ME network. */
    @Override
    public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        Object tile = accessor.getTileEntity();
        if (!chestOrDriveClass.isInstance(tile)) {
            return;
        }

        try {
            int slots = Math.max(0, ((Number) getCellCount.invoke(tile)).intValue());
            int installed = 0;
            int empty = 0;
            int used = 0;
            int full = 0;
            int items = 0;
            int fluids = 0;
            int essentia = 0;
            for (int slot = 0; slot < slots; slot++) {
                int status = ((Number) getCellStatus.invoke(tile, slot)).intValue();
                if (status <= 0) {
                    continue;
                }
                installed++;
                if (status == 1) {
                    empty++;
                } else if (status >= 4) {
                    full++;
                } else {
                    used++;
                }

                int type = ((Number) getCellType.invoke(tile, slot)).intValue();
                if (type == 1) {
                    fluids++;
                } else if (type == 2) {
                    essentia++;
                } else {
                    items++;
                }
            }
            data.setInteger("AE2Slots", slots);
            data.setInteger("AE2Installed", installed);
            data.setInteger("AE2Empty", empty);
            data.setInteger("AE2Used", used);
            data.setInteger("AE2Full", full);
            data.setInteger("AE2Items", items);
            data.setInteger("AE2Fluids", fluids);
            data.setInteger("AE2Essentia", essentia);
            if (byteApi != null) {
                byteApi.appendServerData(data, tile);
            }
        } catch (IllegalAccessException | InvocationTargetException exception) {
            WailaExceptionHandler.handleErr(exception, getClass().getName(), null);
        }
    }

    /** Builds one themed label/value row. */
    private static IComponent value(String labelKey, String value) {
        return new HPanelComponent().text(StatCollector.translateToLocal(labelKey))
                .child(ThemeHelper.INSTANCE.info(": " + value));
    }

    /** Returns the stable AE2 summary provider identifier. */
    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    /** Reflective adapter for AE2's optional public local-cell byte cache API. */
    private static final class ByteApi {

        private static final String STACK_TYPE_REGISTRY = "appeng.api.storage.data.AEStackTypeRegistry";
        private static final String STACK_TYPE = "appeng.api.storage.data.IAEStackType";
        private static final String INVENTORY_HANDLER = "appeng.api.storage.IMEInventoryHandler";
        private static final String CELL_CACHE = "appeng.api.storage.ICellCacheRegistry";

        private final Method getAllTypes;
        private final Method getCellArray;
        private final Method getInternal;
        private final Class<?> cellCacheClass;
        private final Method canGetInventory;
        private final Method getTotalBytes;
        private final Method getUsedBytes;
        private boolean enabled = true;

        /** Resolves every byte-summary method once during optional plugin loading. */
        private ByteApi(Class<?> chestOrDriveClass) throws ReflectiveOperationException {
            Class<?> registryClass = Class.forName(STACK_TYPE_REGISTRY);
            Class<?> stackTypeClass = Class.forName(STACK_TYPE);
            Class<?> inventoryHandlerClass = Class.forName(INVENTORY_HANDLER);
            cellCacheClass = Class.forName(CELL_CACHE);
            getAllTypes = registryClass.getMethod("getAllTypes");
            getCellArray = chestOrDriveClass.getMethod("getCellArray", stackTypeClass);
            getInternal = inventoryHandlerClass.getMethod("getInternal");
            canGetInventory = cellCacheClass.getMethod("canGetInv");
            getTotalBytes = cellCacheClass.getMethod("getTotalBytes");
            getUsedBytes = cellCacheClass.getMethod("getUsedBytes");
        }

        /** Creates the byte adapter only on AE2 versions that expose the public cache API. */
        @Nullable
        private static ByteApi create(Class<?> chestOrDriveClass) {
            try {
                return new ByteApi(chestOrDriveClass);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }

        /** Sums byte usage across this block's local cell handlers without querying its ME network. */
        private void appendServerData(NBTTagCompound data, Object tile) {
            if (!enabled) {
                return;
            }
            try {
                Object stackTypes = getAllTypes.invoke(null);
                if (!(stackTypes instanceof Iterable<?> iterable)) {
                    return;
                }
                Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
                long totalBytes = 0L;
                long usedBytes = 0L;
                for (Object stackType : iterable) {
                    Object handlers = getCellArray.invoke(tile, stackType);
                    if (!(handlers instanceof Iterable<?> handlerIterable)) {
                        continue;
                    }
                    for (Object handler : handlerIterable) {
                        Object cache = cellCacheClass.isInstance(handler) ? handler : getInternal.invoke(handler);
                        if (!cellCacheClass.isInstance(cache) || !visited.add(cache)
                                || !((Boolean) canGetInventory.invoke(cache))) {
                            continue;
                        }
                        totalBytes = saturatedAdd(totalBytes, ((Number) getTotalBytes.invoke(cache)).longValue());
                        usedBytes = saturatedAdd(usedBytes, ((Number) getUsedBytes.invoke(cache)).longValue());
                    }
                }
                if (totalBytes > 0L) {
                    data.setLong("AE2TotalBytes", totalBytes);
                    data.setLong("AE2UsedBytes", Math.min(Math.max(0L, usedBytes), totalBytes));
                }
            } catch (ReflectiveOperationException | ClassCastException exception) {
                enabled = false;
                WailaExceptionHandler.handleErr(exception, getClass().getName(), null);
            }
        }

        /** Adds non-negative capacities while saturating instead of overflowing a long. */
        private static long saturatedAdd(long current, long value) {
            if (value <= 0L) {
                return current;
            }
            return Long.MAX_VALUE - current < value ? Long.MAX_VALUE : current + value;
        }
    }
}

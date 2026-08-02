package com.gtnewhorizons.wdmla.plugin.universal;

import static com.gtnewhorizons.wdmla.impl.ui.component.TooltipComponent.DEFAULT_PROGRESS_DESCRIPTION_PADDING;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.wdmla.ClientProxy;
import com.gtnewhorizons.wdmla.CommonProxy;
import com.gtnewhorizons.wdmla.api.Identifiers;
import com.gtnewhorizons.wdmla.api.TooltipPosition;
import com.gtnewhorizons.wdmla.api.accessor.Accessor;
import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.accessor.EntityAccessor;
import com.gtnewhorizons.wdmla.api.provider.IClientExtensionProvider;
import com.gtnewhorizons.wdmla.api.provider.IComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerExtensionProvider;
import com.gtnewhorizons.wdmla.api.ui.IComponent;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.api.view.ClientViewGroup;
import com.gtnewhorizons.wdmla.api.view.FluidView;
import com.gtnewhorizons.wdmla.api.view.ViewGroup;
import com.gtnewhorizons.wdmla.config.General;
import com.gtnewhorizons.wdmla.config.PluginsConfig;
import com.gtnewhorizons.wdmla.impl.WDMlaClientRegistration;
import com.gtnewhorizons.wdmla.impl.WDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;
import com.gtnewhorizons.wdmla.impl.ui.component.FluidComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.HPanelComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.ProgressComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.VPanelComponent;
import com.gtnewhorizons.wdmla.impl.ui.drawable.FluidDrawable;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Padding;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Size;
import com.gtnewhorizons.wdmla.impl.ui.style.ProgressStyle;
import com.gtnewhorizons.wdmla.util.FormatUtil;

public class FluidStorageProvider<T extends Accessor> implements IComponentProvider<T>, IServerDataProvider<T> {

    private static final String GREGTECH_BASE_META_TILE_ENTITY =
            "gregtech.api.metatileentity.BaseMetaTileEntity";
    private static final String GREGTECH_BASE_META_PIPE_ENTITY =
            "gregtech.api.metatileentity.BaseMetaPipeEntity";

    public static ForBlock getBlock() {
        return ForBlock.INSTANCE;
    }

    public static ForEntity getEntity() {
        return ForEntity.INSTANCE;
    }

    public static class ForBlock extends FluidStorageProvider<BlockAccessor> {

        private static final ForBlock INSTANCE = new ForBlock();
    }

    public static class ForEntity extends FluidStorageProvider<EntityAccessor> {

        private static final ForEntity INSTANCE = new ForEntity();
    }

    @Override
    public void appendTooltip(ITooltip tooltip, T accessor) {
        List<ClientViewGroup<FluidView>> groups = ClientProxy.mapToClientGroups(
                accessor,
                Identifiers.FLUID_STORAGE,
                FluidStorageProvider::decodeGroups,
                WDMlaClientRegistration.instance().fluidStorageProviders::get);
        if (groups == null || groups.isEmpty()) {
            return;
        }

        append(tooltip, accessor, groups);
    }

    /** Adds configured fluid storage rows to the supplied tooltip. */
    public void append(ITooltip tooltip, T accessor, List<ClientViewGroup<FluidView>> groups) {
        if (!accessor.showDetails() && isDetailsOnly(accessor)) {
            return;
        }

        List<ClientViewGroup<FluidView>> visibleGroups = visibleGroups(groups, accessor.showDetails());
        if (visibleGroups.isEmpty()) {
            return;
        }

        boolean renderGroup = visibleGroups.size() > 1 || visibleGroups.get(0).shouldRenderGroup();
        ClientViewGroup.tooltip(tooltip, visibleGroups, renderGroup, (theTooltip, group) -> {
            if (renderGroup) {
                group.renderHeader(theTooltip);
            }
            for (var view : group.views) {
                PluginsConfig.Universal.FluidStorage.Mode showMode = getShowMode();
                boolean showCapacity = accessor.showDetails()
                        && showMode != PluginsConfig.Universal.FluidStorage.Mode.GAUGE;
                IComponent description = buildDescription(view, showCapacity);
                switch (showMode) {
                    case GAUGE -> appendGauge(theTooltip, view, description);
                    case ICON_TEXT -> appendIconText(theTooltip, view, description);
                    case TEXT -> theTooltip.child(description);
                }
            }
            if (group.extraData != null) {
                int extra = group.extraData.getInteger("+");
                if (extra > 0) {
                    theTooltip.text(StatCollector.translateToLocalFormatted("hud.msg.wdmla.more.tanks", extra));
                }
            }
        });
    }

    /** Hides empty tanks normally while retaining the complete group metadata for detailed display. */
    private static List<ClientViewGroup<FluidView>> visibleGroups(List<ClientViewGroup<FluidView>> groups,
            boolean showDetails) {
        if (showDetails) {
            return groups;
        }

        List<ClientViewGroup<FluidView>> visibleGroups = new ArrayList<>();
        for (ClientViewGroup<FluidView> group : groups) {
            List<FluidView> visibleViews = new ArrayList<>();
            for (FluidView view : group.views) {
                if (!isEmptyTank(view)) {
                    visibleViews.add(view);
                }
            }
            if (visibleViews.isEmpty()) {
                continue;
            }

            ClientViewGroup<FluidView> visibleGroup = new ClientViewGroup<>(visibleViews);
            visibleGroup.title = group.title;
            visibleGroup.messageType = group.messageType;
            visibleGroup.boxProgress = group.boxProgress;
            visibleGroup.extraData = group.extraData;
            visibleGroups.add(visibleGroup);
        }
        return visibleGroups;
    }

    /** Detects a decoded tank that has capacity but currently contains no fluid. */
    private static boolean isEmptyTank(FluidView view) {
        return view.current <= 0L;
    }

    /** Resolves the configured display mode, with a safe gauge fallback for missing config values. */
    private static PluginsConfig.Universal.FluidStorage.Mode getShowMode() {
        if (General.forceLegacy) {
            return PluginsConfig.Universal.FluidStorage.Mode.TEXT;
        }
        PluginsConfig.Universal.FluidStorage.Mode mode = PluginsConfig.universal.fluidStorage.mode;
        return mode == null ? PluginsConfig.Universal.FluidStorage.Mode.GAUGE : mode;
    }

    /** Builds the shared fluid name and amount line used by all three display modes. */
    private static IComponent buildDescription(FluidView view, boolean showCapacity) {
        if (view.description != null) {
            return view.description;
        }

        ThemeHelper helper = ThemeHelper.INSTANCE;
        String name = view.fluidName == null ? StatCollector.translateToLocal("hud.msg.wdmla.empty")
                : FormatUtil.formatNameByPixelCount(view.fluidName);
        String current = formatAmount(view.current);
        HPanelComponent description = new HPanelComponent();
        description.child(helper.info(name)).text(": ").child(helper.info(current));
        if (showCapacity) {
            description.text(" / ").text(formatAmount(view.max));
        }
        return description;
    }

    /** Formats a fluid quantity in localized millibuckets. */
    private static String formatAmount(long amount) {
        return FormatUtil.STANDARD.format(amount) + StatCollector.translateToLocal("hud.wdmla.msg.millibucket");
    }

    /** Adds a fluid-textured progress gauge with its amount label overlaid. */
    private static void appendGauge(ITooltip tooltip, FluidView view, IComponent description) {
        ProgressStyle progressStyle = new ProgressStyle().singleColor(General.progressColor.filled)
                .overlay(new FluidDrawable(view.overlay));
        if (view.hasScale) {
            progressStyle.color(General.progressColor.filled, General.progressColor.border);
        }
        tooltip.child(
                new ProgressComponent(view.current, view.max).style(progressStyle)
                        .child(
                                new VPanelComponent().padding(DEFAULT_PROGRESS_DESCRIPTION_PADDING)
                                        .child(description)));
    }

    /** Adds a compact fluid icon followed by the shared text description. */
    private static void appendIconText(ITooltip tooltip, FluidView view, IComponent description) {
        ITooltip row = tooltip.horizontal();
        Size iconSize = new Size(description.getHeight(), description.getHeight());
        if (view.overlay != null) {
            row.child(new FluidComponent(view.overlay).size(iconSize));
        } else {
            row.item(new ItemStack(Items.bucket), new Padding(), iconSize);
        }
        row.child(description);
    }

    @Override
    public void appendServerData(NBTTagCompound data, T accessor) {
        Map.Entry<ResourceLocation, List<ViewGroup<FluidView.Data>>> entry = CommonProxy
                .getServerExtensionData(accessor, WDMlaCommonRegistration.instance().fluidStorageProviders);
        if (entry != null) {
            data.setTag(Identifiers.FLUID_STORAGE.toString(), encodeGroups(entry));
        }
    }

    public static NBTTagCompound encodeGroups(Map.Entry<ResourceLocation, List<ViewGroup<FluidView.Data>>> entry) {
        List<ViewGroup<FluidView.Data>> viewGroups = entry.getValue();
        NBTTagList groupsNBT = new NBTTagList();
        for (ViewGroup<FluidView.Data> viewGroup : viewGroups) {
            groupsNBT.appendTag(encodeGroup(viewGroup));
        }
        NBTTagCompound root = new NBTTagCompound();
        root.setTag(entry.getKey().toString(), groupsNBT);

        return root;
    }

    public static NBTTagCompound encodeGroup(ViewGroup<FluidView.Data> viewGroup) {
        List<NBTTagCompound> encodedFluidData = new ArrayList<>();
        for (FluidView.Data fluidData : viewGroup.views) {
            encodedFluidData.add(FluidView.Data.encode(fluidData));
        }
        ViewGroup<NBTTagCompound> contentEncodedGroup = new ViewGroup<>(encodedFluidData, viewGroup);
        return ViewGroup.encode(contentEncodedGroup);
    }

    public static Map.Entry<ResourceLocation, List<ViewGroup<FluidView.Data>>> decodeGroups(NBTTagCompound root) {
        if (root.hasNoTags()) {
            return null;
        }

        String key = root.func_150296_c().iterator().next();
        ResourceLocation resourceLocation = new ResourceLocation(key);

        NBTTagList groupsNBT = root.getTagList(key, 10);
        List<ViewGroup<FluidView.Data>> viewGroups = new ArrayList<>();
        for (int i = 0; i < groupsNBT.tagCount(); i++) {
            NBTTagCompound groupNBT = groupsNBT.getCompoundTagAt(i);
            viewGroups.add(decodeGroup(groupNBT));
        }

        return new AbstractMap.SimpleEntry<>(resourceLocation, viewGroups);
    }

    public static ViewGroup<FluidView.Data> decodeGroup(NBTTagCompound groupNBT) {
        ViewGroup<NBTTagCompound> contentDecodedGroup = ViewGroup.decode(groupNBT);

        List<FluidView.Data> fluidDataList = new ArrayList<>();
        for (NBTTagCompound fluidNBT : contentDecodedGroup.views) {
            fluidDataList.add(FluidView.Data.decode(fluidNBT));
        }

        return new ViewGroup<>(fluidDataList, contentDecodedGroup);
    }

    @Override
    public ResourceLocation getUid() {
        return Identifiers.FLUID_STORAGE;
    }

    @Override
    public int getDefaultPriority() {
        return TooltipPosition.TAIL + 200;
    }

    @Override
    public boolean shouldRequestData(T accessor) {
        return (accessor.showDetails() || !isDetailsOnly(accessor))
                && accessor.getTarget() != null
                && !WDMlaCommonRegistration.instance().fluidStorageProviders.wrappedGet(accessor).isEmpty();
    }

    /** Keeps GregTech machine and pipe tanks visible normally while honoring Detailed-Only for other integrations. */
    private static boolean isDetailsOnly(Accessor accessor) {
        return PluginsConfig.universal.fluidStorage.detailed && !isGregTechMetaTile(accessor.getTarget());
    }

    /** Detects GregTech's machine and pipe wrappers without loading GregTech classes when the mod is absent. */
    private static boolean isGregTechMetaTile(@Nullable Object target) {
        for (Class<?> type = target == null ? null : target.getClass(); type != null; type = type.getSuperclass()) {
            String className = type.getName();
            if (GREGTECH_BASE_META_TILE_ENTITY.equals(className) || GREGTECH_BASE_META_PIPE_ENTITY.equals(className)) {
                return true;
            }
        }
        return false;
    }

    public enum Extension
            implements IServerExtensionProvider<FluidView.Data>, IClientExtensionProvider<FluidView.Data, FluidView> {

        INSTANCE;

        @Override
        public ResourceLocation getUid() {
            return Identifiers.FLUID_STORAGE_DEFAULT;
        }

        @Override
        public List<ClientViewGroup<FluidView>> getClientGroups(Accessor accessor,
                List<ViewGroup<FluidView.Data>> groups) {
            return ClientViewGroup.map(groups, FluidView::readDefault, null);
        }

        @Nullable
        @Override
        public List<ViewGroup<FluidView.Data>> getGroups(Accessor accessor) {
            return CommonProxy.wrapFluidStorage(accessor);
        }

        @Override
        public boolean shouldRequestData(Accessor accessor) {
            return true; // I need to change this when I want to apply this to every TE
        }

        @Override
        public int getDefaultPriority() {
            return 9999;
        }
    }
}

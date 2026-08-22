package com.gtnewhorizons.wdmla.plugin.gregtech;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;

import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.wdmla.CommonProxy;
import com.gtnewhorizons.wdmla.api.accessor.Accessor;
import com.gtnewhorizons.wdmla.api.provider.IClientExtensionProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerExtensionProvider;
import com.gtnewhorizons.wdmla.api.view.ClientViewGroup;
import com.gtnewhorizons.wdmla.api.view.FluidView;
import com.gtnewhorizons.wdmla.api.view.ViewGroup;

import mcp.mobius.waila.utils.WailaExceptionHandler;

/** Reads the real tier capacity of GregTech Super and Quantum Tanks. */
public final class GregTechDigitalTankStorageProvider implements IServerExtensionProvider<FluidView.Data> {

    private static final String DIGITAL_TANK = "gregtech.common.tileentities.storage.MTEDigitalTankBase";
    static final ResourceLocation DIGITAL_TANK_STORAGE = new ResourceLocation("gregtech", "digital_tank_storage");

    private final Class<?> digitalTankClass;
    private final Method getMetaTileEntity;
    private final Method getRealTankInfo;

    /** Resolves and caches GregTech's digital tank methods during optional plugin registration. */
    private GregTechDigitalTankStorageProvider(Class<?> baseMetaTileEntity) throws ReflectiveOperationException {
        digitalTankClass = Class.forName(DIGITAL_TANK);
        getMetaTileEntity = baseMetaTileEntity.getMethod("getMetaTileEntity");
        getRealTankInfo = digitalTankClass.getMethod("getRealTankInfo", ForgeDirection.class);
    }

    /** Creates the digital tank adapter when the installed GregTech API exposes real tank capacity. */
    @Nullable
    public static GregTechDigitalTankStorageProvider create(Class<?> baseMetaTileEntity) {
        try {
            return new GregTechDigitalTankStorageProvider(baseMetaTileEntity);
        } catch (ReflectiveOperationException exception) {
            WailaExceptionHandler.handleErr(exception, GregTechDigitalTankStorageProvider.class.getName(), null);
            return null;
        }
    }

    /** Returns real-capacity tank data only for the dedicated digital tank hierarchy. */
    @Nullable
    @Override
    public List<ViewGroup<FluidView.Data>> getGroups(Accessor accessor) {
        Object target = accessor.getTarget();
        if (target == null) {
            return null;
        }
        try {
            Object metaTileEntity = getMetaTileEntity.invoke(target);
            if (!digitalTankClass.isInstance(metaTileEntity)) {
                return null;
            }
            FluidTankInfo[] tanks =
                    (FluidTankInfo[]) getRealTankInfo.invoke(metaTileEntity, ForgeDirection.UNKNOWN);
            return CommonProxy.fromFluidStorage(tanks);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            WailaExceptionHandler.handleErr(exception, getClass().getName(), null);
            return null;
        }
    }

    /** Selects the dedicated GregTech decoder without colliding with the generic fluid fallback. */
    @Override
    public ResourceLocation getUid() {
        return DIGITAL_TANK_STORAGE;
    }

    /** Makes the real digital tank capacity win over the generic GregTech fluid handler. */
    @Override
    public int getDefaultPriority() {
        return 1000;
    }

    /** Maps GregTech's standard FluidView payload under its dedicated provider identifier. */
    public enum ClientExtension implements IClientExtensionProvider<FluidView.Data, FluidView> {

        INSTANCE;

        /** Decodes the synchronized tank rows with WDMla's standard fluid mapping. */
        @Override
        public List<ClientViewGroup<FluidView>> getClientGroups(Accessor accessor,
                List<ViewGroup<FluidView.Data>> groups) {
            return ClientViewGroup.map(groups, FluidView::readDefault, null);
        }

        /** Matches the dedicated identifier emitted by the GregTech server provider. */
        @Override
        public ResourceLocation getUid() {
            return DIGITAL_TANK_STORAGE;
        }
    }
}

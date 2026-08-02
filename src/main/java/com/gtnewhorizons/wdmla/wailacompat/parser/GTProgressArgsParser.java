package com.gtnewhorizons.wdmla.wailacompat.parser;

import java.lang.reflect.Method;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.wdmla.api.ITTRenderParser;
import com.gtnewhorizons.wdmla.api.accessor.Accessor;
import com.gtnewhorizons.wdmla.impl.ObjectDataCenter;
import com.gtnewhorizons.wdmla.impl.ui.component.Component;
import com.gtnewhorizons.wdmla.impl.ui.component.HPanelComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.IconComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.ProgressComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.TextComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.VPanelComponent;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Padding;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Size;
import com.gtnewhorizons.wdmla.impl.ui.style.PanelStyle;
import com.gtnewhorizons.wdmla.overlay.WDMlaUIIcons;

public class GTProgressArgsParser implements ITTRenderParser {

    private static final long NANOS_PER_TICK = 50_000_000L;
    private static final long MAX_PREDICTION_NANOS = (ObjectDataCenter.RATE_LIMIT_MP + 100L) * 1_000_000L;

    private Object trackedTarget;
    private NBTTagCompound lastServerSnapshot;
    private int authoritativeProgress;
    private int authoritativeMaximum;
    private long authoritativeSampleTime;
    private double progressTicksPerNanosecond = 1.0 / NANOS_PER_TICK;
    private boolean predicting;
    private Class<?> activeMethodOwner;
    private Method activeMethod;

    /** Converts GregTech's three-argument Waila progress token into WDMla progress and status components. */
    @Override
    public Component parse(String[] args) {
        int progressTime = Integer.parseInt(args[0]);
        int maxProgressTime = Integer.parseInt(args[1]);
        boolean allowedToWork = Boolean.parseBoolean(args[2]);
        Accessor accessor = ObjectDataCenter.get();
        Object target = accessor == null ? null : accessor.getTarget();
        NBTTagCompound serverSnapshot = ObjectDataCenter.getServerData();
        double displayedProgress = updateInterpolatedProgress(
                progressTime,
                maxProgressTime,
                allowedToWork,
                target,
                serverSnapshot,
                System.nanoTime());
        double ratio = maxProgressTime == 0 ? 0.0 : displayedProgress / maxProgressTime;
        ratio = Math.max(0.0, Math.min(ratio, 1.0));

        String progressText = StatCollector.translateToLocalFormatted(
                "GT5U.waila.machine.in_progress",
                displayedProgress / 20.0,
                maxProgressTime / 20.0,
                Math.round(ratio * 1000.0) / 10.0);
        VPanelComponent result = new VPanelComponent();
        result.style(new PanelStyle().spacing(2));
        result.child(
                new ProgressComponent(Math.round(displayedProgress), Math.max(0, maxProgressTime)).child(
                        new TextComponent(progressText)
                                .padding(ProgressComponent.DEFAULT_PROGRESS_DESCRIPTION_PADDING)));
        if (!allowedToWork) {
            result.child(buildDisabledStatus());
        }
        return result;
    }

    /** Predicts progress between authoritative Waila snapshots without increasing the server request rate. */
    private double updateInterpolatedProgress(int progress, int maximum, boolean allowedToWork, Object target,
            NBTTagCompound serverSnapshot, long now) {
        int clampedMaximum = Math.max(0, maximum);
        int clampedProgress = Math.max(0, Math.min(progress, clampedMaximum));
        boolean reset = target != trackedTarget
                || clampedMaximum != authoritativeMaximum
                || clampedProgress < authoritativeProgress;

        if (reset) {
            resetInterpolation(clampedProgress, clampedMaximum, allowedToWork, target, serverSnapshot, now);
        } else if (serverSnapshot != lastServerSnapshot) {
            long sampleInterval = Math.max(0L, now - authoritativeSampleTime);
            int sampleAdvance = clampedProgress - authoritativeProgress;
            authoritativeProgress = clampedProgress;
            authoritativeSampleTime = now;
            lastServerSnapshot = serverSnapshot;
            if (sampleAdvance > 0 && sampleInterval > 0L) {
                progressTicksPerNanosecond = sampleAdvance / (double) sampleInterval;
                predicting = allowedToWork && clampedProgress < clampedMaximum && isTargetActive(target);
            } else {
                predicting = false;
            }
        }

        if (!allowedToWork || !isTargetActive(target)) {
            predicting = false;
        }
        if (!predicting) {
            return clampedProgress;
        }

        long predictionTime = Math.min(Math.max(0L, now - authoritativeSampleTime), MAX_PREDICTION_NANOS);
        return Math.min(clampedMaximum, authoritativeProgress + predictionTime * progressTicksPerNanosecond);
    }

    /** Resets prediction when the target or recipe changes and seeds the normal 20 TPS rate. */
    private void resetInterpolation(int progress, int maximum, boolean allowedToWork, Object target,
            NBTTagCompound serverSnapshot, long now) {
        trackedTarget = target;
        lastServerSnapshot = serverSnapshot;
        authoritativeProgress = progress;
        authoritativeMaximum = maximum;
        authoritativeSampleTime = now;
        progressTicksPerNanosecond = 1.0 / NANOS_PER_TICK;
        predicting = allowedToWork && progress < maximum && isTargetActive(target);
    }

    /** Reads GregTech's synchronized client active flag when that optional API is present. */
    private boolean isTargetActive(Object target) {
        if (target == null) {
            return true;
        }

        Class<?> targetClass = target.getClass();
        if (targetClass != activeMethodOwner) {
            activeMethodOwner = targetClass;
            activeMethod = null;
            try {
                Method method = targetClass.getMethod("isActive");
                if (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class) {
                    activeMethod = method;
                }
            } catch (NoSuchMethodException | SecurityException ignored) {
                return true;
            }
        }
        if (activeMethod == null) {
            return true;
        }

        try {
            return Boolean.TRUE.equals(activeMethod.invoke(target));
        } catch (ReflectiveOperationException | SecurityException ignored) {
            activeMethod = null;
            return true;
        }
    }

    /** Builds the modern pause-icon row used when GregTech working is disabled. */
    private static Component buildDisabledStatus() {
        HPanelComponent status = new HPanelComponent();
        status.style(new PanelStyle().spacing(2));
        status.child(
                new IconComponent(WDMlaUIIcons.PAUSE, WDMlaUIIcons.PAUSE.texPath).padding(new Padding())
                        .size(new Size(9, 9)));
        status.child(new TextComponent(StatCollector.translateToLocal("GT5U.waila.machine.working_disabled")));
        return status;
    }
}

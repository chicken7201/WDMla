package com.gtnewhorizons.wdmla.impl.ui.value;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.gtnewhorizons.wdmla.api.ui.IFilledProgress;

/** Reuses smooth progress values across HUD rebuilds and advances them at render-frame frequency. */
public final class ProgressAnimationTracker {

    private static final long PROGRESS_SCALE = 1_000_000L;
    private static final double RESPONSE_NANOS = 120_000_000.0;
    private static final double RESET_THRESHOLD = 0.5;
    private static final double SNAP_EPSILON = 0.000_01;

    private static final List<SmoothProgress> TRACKED_PROGRESS = new ArrayList<>();
    private static WeakReference<Object> lastTarget = new WeakReference<>(null);
    private static int nextIndex;
    private static boolean collecting;

    private ProgressAnimationTracker() {}

    /** Starts assigning stable progress slots for one rebuilt HUD. */
    public static void beginFrame(@Nullable Object target) {
        if (lastTarget.get() != target) {
            TRACKED_PROGRESS.clear();
        }
        lastTarget = new WeakReference<>(target);
        nextIndex = 0;
        collecting = true;
    }

    /** Returns the stable animated value assigned to the next progress component. */
    public static IFilledProgress track(long current, long maximum) {
        if (!collecting) {
            return new FilledProgress(current, maximum);
        }

        SmoothProgress progress;
        if (nextIndex < TRACKED_PROGRESS.size()) {
            progress = TRACKED_PROGRESS.get(nextIndex);
        } else {
            progress = new SmoothProgress();
            TRACKED_PROGRESS.add(progress);
        }
        nextIndex++;
        progress.setTarget(current, maximum);
        return progress;
    }

    /** Finishes slot assignment and discards progress states no longer present in the HUD. */
    public static void endFrame() {
        while (TRACKED_PROGRESS.size() > nextIndex) {
            TRACKED_PROGRESS.remove(TRACKED_PROGRESS.size() - 1);
        }
        collecting = false;
    }

    /** Stores one normalized progress target and eases its displayed value over real render time. */
    private static final class SmoothProgress implements IFilledProgress {

        private double displayedRatio;
        private double targetRatio;
        private long sourceMaximum;
        private long lastUpdateTime;
        private boolean initialized;

        /** Validates and applies a newer raw progress sample. */
        private void setTarget(long current, long maximum) {
            if (current < 0) {
                current = 0;
            }
            if (maximum < 0) {
                throw new IllegalArgumentException("Max Progress cannot below zero.");
            }
            if (current > maximum) {
                throw new IllegalArgumentException("Current progress cannot exceed max progress.");
            }

            long now = System.nanoTime();
            advance(now);
            double ratio = maximum == 0 ? 0.0 : (double) current / maximum;
            boolean reset = !initialized
                    || maximum != sourceMaximum
                    || ratio == 0.0 && targetRatio > 0.0
                    || targetRatio - ratio > RESET_THRESHOLD;
            if (reset) {
                displayedRatio = ratio;
                initialized = true;
            }
            targetRatio = ratio;
            sourceMaximum = maximum;
            lastUpdateTime = now;
        }

        /** Returns the frame-interpolated current value at fixed precision. */
        @Override
        public long getCurrent() {
            advance(System.nanoTime());
            return Math.round(displayedRatio * PROGRESS_SCALE);
        }

        /** Returns the fixed precision used by every normalized animation state. */
        @Override
        public long getMax() {
            return PROGRESS_SCALE;
        }

        /** Advances the current value using time-based exponential smoothing. */
        private void advance(long now) {
            if (!initialized) {
                lastUpdateTime = now;
                return;
            }
            long elapsed = Math.max(0L, now - lastUpdateTime);
            if (elapsed == 0L || displayedRatio == targetRatio) {
                return;
            }
            double response = 1.0 - Math.exp(-elapsed / RESPONSE_NANOS);
            displayedRatio += (targetRatio - displayedRatio) * response;
            if (Math.abs(targetRatio - displayedRatio) <= SNAP_EPSILON) {
                displayedRatio = targetRatio;
            }
            displayedRatio = Math.max(0.0, Math.min(displayedRatio, 1.0));
            lastUpdateTime = now;
        }
    }
}

package dev.cubimised.api.client;

import java.util.Arrays;

/**
 * Lightweight rolling frame-time monitor.
 * Uses render-frame timestamps instead of Minecraft's once-per-second FPS counter.
 */
public final class PerformanceMonitor {
    private static final int WINDOW = 240;
    private final double[] frameTimes = new double[WINDOW];
    private int cursor;
    private int count;
    private long lastFrameNanos;
    private double averageFps;
    private double onePercentLow;
    private double pointOnePercentLow;
    private double minFps;
    private double maxFps;

    public void recordFrame() {
        long now = System.nanoTime();
        if (lastFrameNanos != 0L) {
            double ms = Math.max(0.05, Math.min(1000.0, (now - lastFrameNanos) / 1_000_000.0));
            frameTimes[cursor] = ms;
            cursor = (cursor + 1) % WINDOW;
            if (count < WINDOW) count++;
            recalculate();
        }
        lastFrameNanos = now;
    }

    private void recalculate() {
        double totalMs = 0.0;
        double min = Double.MAX_VALUE;
        double max = 0.0;
        for (int i = 0; i < count; i++) {
            double ms = frameTimes[i];
            totalMs += ms;
            min = Math.min(min, ms);
            max = Math.max(max, ms);
        }
        averageFps = totalMs <= 0.0 ? 0.0 : count * 1000.0 / totalMs;
        minFps = max <= 0.0 ? 0.0 : 1000.0 / max;
        maxFps = min <= 0.0 ? 0.0 : 1000.0 / min;

        double[] sorted = Arrays.copyOf(frameTimes, count);
        Arrays.sort(sorted);
        onePercentLow = percentileFps(sorted, 0.99);
        pointOnePercentLow = percentileFps(sorted, 0.999);
    }

    private double percentileFps(double[] sortedMs, double percentile) {
        if (sortedMs.length == 0) return 0.0;
        int index = Math.min(sortedMs.length - 1, (int)Math.floor(sortedMs.length * percentile));
        return 1000.0 / Math.max(0.05, sortedMs[index]);
    }

    public double getAverageFps() { return averageFps; }
    public double getOnePercentLow() { return onePercentLow; }
    public double getPointOnePercentLow() { return pointOnePercentLow; }
    public double getMinFps() { return minFps; }
    public double getMaxFps() { return maxFps; }
    public int getSampleCount() { return count; }
}

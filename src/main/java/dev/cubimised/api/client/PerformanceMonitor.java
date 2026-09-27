package dev.cubimised.api.client;

import dev.cubimised.api.CubimisedApi;
import net.minecraft.client.MinecraftClient;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;

/** Lightweight rolling frame-time and hardware/network telemetry monitor. */
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

    /** Refresh slower telemetry so the render thread is not burdened every frame. */
    public void updateTelemetry(MinecraftClient client) {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        CubimisedApi.systemCpuUsagePercent = readLoad(os, "getCpuLoad");
        CubimisedApi.cpuUsagePercent = readLoad(os, "getProcessCpuLoad");

        if (client.getNetworkHandler() != null && client.player != null) {
            var entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            CubimisedApi.pingMs = entry == null ? -1 : entry.getLatency();
        } else {
            CubimisedApi.pingMs = -1;
        }
        // Server TPS is not reliably exposed to a vanilla client, so do not fake it.
        CubimisedApi.serverTps = -1.0;
    }

    private double readLoad(OperatingSystemMXBean bean, String methodName) {
        try {
            Object value = bean.getClass().getMethod(methodName).invoke(bean);
            if (value instanceof Number number) {
                double load = number.doubleValue();
                return load < 0.0 ? -1.0 : Math.min(100.0, load * 100.0);
            }
        } catch (ReflectiveOperationException | SecurityException ignored) {
            // Android and non-HotSpot JVMs may not expose the optional MXBean methods.
        }
        return -1.0;
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
        int index = Math.min(sortedMs.length - 1, (int) Math.floor(sortedMs.length * percentile));
        return 1000.0 / Math.max(0.05, sortedMs[index]);
    }

    public double getAverageFps() { return averageFps; }
    public double getOnePercentLow() { return onePercentLow; }
    public double getPointOnePercentLow() { return pointOnePercentLow; }
    public double getMinFps() { return minFps; }
    public double getMaxFps() { return maxFps; }
    public int getSampleCount() { return count; }
}

package dev.cubimised.api.client.render;

/** Lightweight frame statistics for the Cubimised renderer dashboard. */
public final class RenderStats {
    private long frameStartNanos;
    private long lastFrameNanos;
    private long frameCount;
    private long totalFrameNanos;

    public void beginFrame() {
        frameStartNanos = System.nanoTime();
    }

    public void endFrame() {
        if (frameStartNanos == 0L) return;
        lastFrameNanos = System.nanoTime() - frameStartNanos;
        totalFrameNanos += lastFrameNanos;
        frameCount++;
    }

    public double lastFrameMs() {
        return lastFrameNanos / 1_000_000.0;
    }

    public double averageFrameMs() {
        return frameCount == 0 ? 0.0 : (totalFrameNanos / (double) frameCount) / 1_000_000.0;
    }

    public long frameCount() {
        return frameCount;
    }
}

package dev.cubimised.api.client.render;

public final class RenderStats {
    private long frameStartNanos;
    private long lastFrameNanos;
    private long frameCount;
    private long totalFrameNanos;
    private long terrainPasses;
    private long chunkPreparationPasses;

    public void beginFrame() { frameStartNanos = System.nanoTime(); }

    public void endFrame() {
        if (frameStartNanos == 0L) return;
        lastFrameNanos = System.nanoTime() - frameStartNanos;
        totalFrameNanos += lastFrameNanos;
        frameCount++;
    }

    public void recordTerrainPass() { terrainPasses++; }
    public void recordChunkPreparationPass() { chunkPreparationPasses++; }
    public void recordChunkPreparationPass(int count) { chunkPreparationPasses += Math.max(0, count); }

    public double lastFrameMs() { return lastFrameNanos / 1_000_000.0; }

    public double averageFrameMs() {
        return frameCount == 0 ? 0.0 : (totalFrameNanos / (double) frameCount) / 1_000_000.0;
    }

    public long frameCount() { return frameCount; }
    public long terrainPasses() { return terrainPasses; }
    public long chunkPreparationPasses() { return chunkPreparationPasses; }
}

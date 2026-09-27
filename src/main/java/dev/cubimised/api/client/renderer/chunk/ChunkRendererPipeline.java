package dev.cubimised.api.client.renderer.chunk;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Coordinates CPU mesh generation and render-thread resource handoff.
 *
 * <p>This is infrastructure for the upcoming terrain backend, not a replacement
 * for Minecraft's renderer yet. A backend must provide immutable inputs to
 * {@link #schedule} and perform its actual GPU upload in the queued callback.</p>
 */
public final class ChunkRendererPipeline implements AutoCloseable {
    private final ChunkBuildScheduler scheduler;
    private final RenderThreadUploadQueue uploadQueue;

    public ChunkRendererPipeline(int workerCount, int queueCapacity,
                                 java.util.concurrent.Executor clientExecutor) {
        scheduler = new ChunkBuildScheduler(workerCount, queueCapacity, clientExecutor);
        uploadQueue = new RenderThreadUploadQueue();
    }

    public Future<?> schedule(Callable<ChunkMeshData> cpuBuild,
                              Consumer<ChunkMeshData> uploadOnClientThread,
                              Consumer<Throwable> onFailure) {
        Objects.requireNonNull(uploadOnClientThread, "uploadOnClientThread");
        return scheduler.submit(cpuBuild, mesh -> uploadQueue.enqueue(() -> uploadOnClientThread.accept(mesh)),
                onFailure);
    }

    /**
     * Call only from the render thread, with a per-frame budget to avoid upload
     * spikes. The caller owns the thread assertion and GPU state setup.
     */
    public int processUploads(int maxUploadsPerFrame) {
        return uploadQueue.drain(maxUploadsPerFrame);
    }

    /** Call on dimension/world replacement to discard results from the old world. */
    public void onWorldChanged() {
        scheduler.invalidatePendingBuilds();
        uploadQueue.clear();
    }

    public int queuedBuildCount() { return scheduler.queuedBuildCount(); }
    public int activeBuildCount() { return scheduler.activeBuildCount(); }
    public int pendingUploadCount() { return uploadQueue.pendingCount(); }

    @Override public void close() {
        scheduler.close();
        uploadQueue.close();
    }
}

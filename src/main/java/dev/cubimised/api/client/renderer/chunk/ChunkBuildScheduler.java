package dev.cubimised.api.client.renderer.chunk;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Bounded asynchronous chunk-mesh build queue.
 *
 * <p>Workers may only produce CPU-side mesh data. The completion callback is
 * dispatched through the supplied client-thread executor so GPU uploads and world
 * access remain the responsibility of the render thread.</p>
 */
public final class ChunkBuildScheduler implements AutoCloseable {
    private final ThreadPoolExecutor workers;
    private final Executor clientExecutor;
    private final AtomicLong generation = new AtomicLong();
    private final AtomicBoolean closed = new AtomicBoolean();

    public ChunkBuildScheduler(int workerCount, int queueCapacity, Executor clientExecutor) {
        if (workerCount < 1) throw new IllegalArgumentException("workerCount must be at least 1");
        if (queueCapacity < 1) throw new IllegalArgumentException("queueCapacity must be at least 1");
        this.clientExecutor = Objects.requireNonNull(clientExecutor, "clientExecutor");
        AtomicLong threadNumber = new AtomicLong();
        ThreadFactory factory = task -> {
            Thread thread = new Thread(task, "Cubimised-ChunkBuilder-" + threadNumber.incrementAndGet());
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            return thread;
        };
        workers = new ThreadPoolExecutor(workerCount, workerCount, 30L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity), factory, new ThreadPoolExecutor.DiscardOldestPolicy());
        workers.allowCoreThreadTimeOut(true);
    }

    /**
     * Submits a CPU-only build. The build function must not access Minecraft world
     * state or OpenGL; capture all required immutable input before submitting.
     */
    public Future<?> submit(Callable<ChunkMeshData> build, Consumer<ChunkMeshData> onClientThread,
                            Consumer<Throwable> onFailure) {
        Objects.requireNonNull(build, "build");
        Objects.requireNonNull(onClientThread, "onClientThread");
        Objects.requireNonNull(onFailure, "onFailure");
        if (closed.get()) throw new IllegalStateException("ChunkBuildScheduler is closed");
        long submittedGeneration = generation.get();
        return workers.submit(() -> {
            if (closed.get() || Thread.currentThread().isInterrupted()) return;
            try {
                ChunkMeshData mesh = build.call();
                if (mesh == null || closed.get() || generation.get() != submittedGeneration) return;
                clientExecutor.execute(() -> {
                    if (!closed.get() && generation.get() == submittedGeneration) {
                        onClientThread.accept(mesh);
                    }
                });
            } catch (Throwable failure) {
                if (!closed.get() && generation.get() == submittedGeneration) {
                    clientExecutor.execute(() -> {
                        if (!closed.get() && generation.get() == submittedGeneration) onFailure.accept(failure);
                    });
                }
            }
        });
    }

    /** Invalidates all queued/in-flight results, e.g. on world change. */
    public void invalidatePendingBuilds() {
        generation.incrementAndGet();
        workers.getQueue().clear();
    }

    public int queuedBuildCount() { return workers.getQueue().size(); }
    public int activeBuildCount() { return workers.getActiveCount(); }
    public boolean isClosed() { return closed.get(); }

    @Override public void close() {
        if (closed.compareAndSet(false, true)) {
            generation.incrementAndGet();
            workers.shutdownNow();
        }
    }
}

package dev.cubimised.api.client.renderer.chunk;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe handoff for GPU/resource work.
 *
 * <p>Enqueue from any thread, but call {@link #drain(int)} only from the Minecraft
 * render thread. Tasks are intentionally generic so this class does not perform
 * OpenGL calls or assume a particular vertex format.</p>
 */
public final class RenderThreadUploadQueue {
    private final Queue<Runnable> pending = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean closed = new AtomicBoolean();

    public boolean enqueue(Runnable uploadTask) {
        Objects.requireNonNull(uploadTask, "uploadTask");
        if (closed.get()) return false;
        pending.add(uploadTask);
        if (closed.get() && pending.remove(uploadTask)) return false;
        return true;
    }

    /** Runs at most maxTasks queued resource operations on the calling thread. */
    public int drain(int maxTasks) {
        if (maxTasks < 0) throw new IllegalArgumentException("maxTasks cannot be negative");
        int completed = 0;
        while (completed < maxTasks) {
            Runnable task = pending.poll();
            if (task == null) break;
            task.run();
            completed++;
        }
        return completed;
    }

    public int pendingCount() { return pending.size(); }

    public void clear() { pending.clear(); }

    public void close() {
        if (closed.compareAndSet(false, true)) pending.clear();
    }

    public boolean isClosed() { return closed.get(); }
}

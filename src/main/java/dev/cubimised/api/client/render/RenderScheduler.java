package dev.cubimised.api.client.render;

import net.minecraft.client.render.Camera;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Allocation-conscious scheduler for render preparation.
 * OpenGL work must remain on the render thread.
 */
public final class RenderScheduler {
    private final PriorityQueue<RenderTask> queue = new PriorityQueue<>(
            Comparator.comparingInt(RenderTask::priority).reversed()
                    .thenComparingDouble(RenderTask::distanceSquared));
    private int budget = 64;
    private int processed;

    public void beginFrame() {
        processed = 0;
        budget = 64;
    }

    public void prepareChunkPass(Camera camera) {
        if (camera == null) return;
    }

    public void endFrame() {
        while (processed < budget && !queue.isEmpty()) {
            queue.poll().run();
            processed++;
        }
    }

    public void enqueue(RenderTask task) {
        if (task != null) queue.offer(task);
    }

    public void clear() { queue.clear(); }
    public int queuedTasks() { return queue.size(); }
    public int processedThisFrame() { return processed; }
}

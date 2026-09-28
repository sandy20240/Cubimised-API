package dev.cubimised.api.client.render;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Small allocation-conscious priority queue for future chunk mesh rebuilds.
 * Higher priority work is processed first; distance is used as a tie breaker.
 */
public final class RenderScheduler {
    private final PriorityQueue<RenderTask> queue = new PriorityQueue<>(
            Comparator.comparingInt(RenderTask::priority).reversed()
                    .thenComparingDouble(RenderTask::distanceSquared));
    private int budget = 64;
    private int processed;

    public void beginFrame() {
        processed = 0;
        // Keep the first implementation conservative. The budget can later be
        // adapted from frame-time telemetry without changing callers.
        budget = 64;
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

    public void clear() {
        queue.clear();
    }

    public int queuedTasks() {
        return queue.size();
    }

    public int processedThisFrame() {
        return processed;
    }
}

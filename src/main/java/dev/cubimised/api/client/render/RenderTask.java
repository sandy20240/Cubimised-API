package dev.cubimised.api.client.render;

/** A deferred render-preparation task. Tasks should avoid touching OpenGL. */
public record RenderTask(int priority, double distanceSquared, Runnable action) {
    public void run() {
        if (action != null) action.run();
    }
}

package dev.cubimised.api.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;

/**
 * Cubimised rendering core foundation.
 *
 * This layer owns render scheduling and visibility statistics without replacing
 * vanilla rendering yet. It is deliberately isolated so the chunk renderer can
 * be introduced without coupling it to the existing performance UI.
 */
public final class CubimisedRenderCore {
    private static final RenderScheduler SCHEDULER = new RenderScheduler();
    private static final RenderStats STATS = new RenderStats();
    private static final CubimisedTerrainRenderer TERRAIN = new CubimisedTerrainRenderer();
    private static boolean initialized;

    private CubimisedRenderCore() {}

    public static void init() {
        initialized = true;
    }

    public static void beginFrame(MinecraftClient client, Camera camera) {
        if (!initialized || client.world == null || camera == null) return;
        STATS.beginFrame();
        SCHEDULER.beginFrame();
    }

    public static void endFrame() {
        if (!initialized) return;
        SCHEDULER.endFrame();
        STATS.endFrame();
    }

    public static RenderScheduler scheduler() {
        return SCHEDULER;
    }

    public static RenderStats stats() {
        return STATS;
    }

    public static CubimisedTerrainRenderer terrain() {
        return TERRAIN;
    }
}

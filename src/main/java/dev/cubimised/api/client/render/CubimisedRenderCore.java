package dev.cubimised.api.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;

/** Coordinates Cubimised terrain meshing, GPU storage, visibility and scheduling. */
public final class CubimisedRenderCore {
    private static final RenderScheduler SCHEDULER = new RenderScheduler();
    private static final RenderStats STATS = new RenderStats();
    private static final CubimisedTerrainRenderer TERRAIN = new CubimisedTerrainRenderer();
    private static final CubimisedChunkRebuildSystem REBUILDS = new CubimisedChunkRebuildSystem();
    private static boolean initialized;
    private static int frame;
    private static Camera currentCamera;

    private CubimisedRenderCore() {}

    public static void init() {
        initialized = true;
    }

    public static void beginFrame(MinecraftClient client, Camera camera) {
        if (!initialized || client.world == null || camera == null) return;
        currentCamera = camera;
        STATS.beginFrame();
        SCHEDULER.beginFrame();
        REBUILDS.beginFrame();

        // Keep the rebuild queue populated around the camera without rebuilding
        // the whole view every frame. The queue deduplicates chunk positions.
        if ((frame++ & 15) == 0) {
            REBUILDS.enqueueAround(camera, Math.max(2, Math.min(16, client.options.getViewDistance().getValue())));
        }
        int rebuilt = REBUILDS.process(client, TERRAIN, CubimisedRenderConfig.rebuildBudget());
        STATS.recordChunkPreparationPass(rebuilt);
    }

    public static void endFrame() {
        if (!initialized) return;
        SCHEDULER.endFrame();
        STATS.endFrame();
    }

    public static void markDirty(net.minecraft.util.math.BlockPos pos) {
        if (pos != null) REBUILDS.markDirty(new net.minecraft.util.math.ChunkPos(pos));
    }

    public static boolean shouldTakeoverTerrain() {
        if (!CubimisedRenderConfig.customTerrainEnabled() || currentCamera == null) return false;
        MinecraftClient client = MinecraftClient.getInstance();
        int radius = Math.max(2, Math.min(16, client.options.getViewDistance().getValue()));
        return TERRAIN.hasCoverage(currentCamera, radius);
    }

    public static boolean renderTerrainLayer(net.minecraft.client.render.RenderLayer layer,
                                              double cameraX, double cameraY, double cameraZ) {
        return TERRAIN.renderLayer(layer, cameraX, cameraY, cameraZ);
    }

    public static RenderScheduler scheduler() { return SCHEDULER; }
    public static RenderStats stats() { return STATS; }
    public static CubimisedTerrainRenderer terrain() { return TERRAIN; }
    public static CubimisedChunkRebuildSystem rebuilds() { return REBUILDS; }
}

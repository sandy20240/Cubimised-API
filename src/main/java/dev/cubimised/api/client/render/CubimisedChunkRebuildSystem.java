package dev.cubimised.api.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.ChunkRendererRegionBuilder;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

/** Dirty/rebuild queue for Cubimised terrain sections. */
public final class CubimisedChunkRebuildSystem {
    private final PriorityQueue<Work> queue = new PriorityQueue<>((a,b) -> Double.compare(a.distanceSq, b.distanceSq));
    private final Set<Long> queued = new HashSet<>();
    private final ChunkRendererRegionBuilder regions = new ChunkRendererRegionBuilder();

    public void beginFrame() {
    }

    public void enqueue(ChunkPos pos, double distanceSq) {
        if (queued.add(pos.toLong())) queue.offer(new Work(pos, distanceSq));
    }

    public void enqueueAround(Camera camera, int radius) {
        if (camera == null) return;
        int cx = ((int)Math.floor(camera.getPos().x)) >> 4;
        int cz = ((int)Math.floor(camera.getPos().z)) >> 4;
        int r = Math.max(1, radius);
        for (int z = cz - r; z <= cz + r; z++) {
            for (int x = cx - r; x <= cx + r; x++) {
                double dx = (x + 0.5) * 16.0 - camera.getPos().x;
                double dz = (z + 0.5) * 16.0 - camera.getPos().z;
                enqueue(new ChunkPos(x, z), dx * dx + dz * dz);
            }
        }
    }

    public int process(MinecraftClient client, CubimisedTerrainRenderer terrain, int budget) {
        if (client.world == null) return 0;
        int processed = 0;
        while (processed < budget && !queue.isEmpty()) {
            Work work = queue.poll();
            queued.remove(work.pos.toLong());
            ClientWorld world = client.world;
            int minY = world.getBottomY();
            int maxY = world.getTopYInclusive();
            BlockPos origin = new BlockPos(work.pos.getStartX(), minY, work.pos.getStartZ());
            BlockPos end = new BlockPos(work.pos.getEndX(), maxY, work.pos.getEndZ());
            ChunkRendererRegion region = regions.build(world, origin, end, 1);
            if (region != null) {
                CubimisedChunkRenderData data = CubimisedChunkMesher.mesh(region, origin, minY, maxY);
                terrain.replaceChunk(work.pos, data);
            }
            processed++;
        }
        return processed;
    }

    public void markDirty(ChunkPos pos) {
        enqueue(pos, 0.0);
    }

    public int queuedCount() { return queue.size(); }

    private record Work(ChunkPos pos, double distanceSq) {}
}

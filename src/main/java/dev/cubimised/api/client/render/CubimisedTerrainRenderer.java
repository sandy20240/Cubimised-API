package dev.cubimised.api.client.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.util.math.ChunkPos;
import org.joml.Matrix4f;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.client.render.RenderLayer;

/**
 * Cubimised terrain GPU backend.
 *
 * Meshes are stored per chunk and render layer. CPU buffers are released after
 * upload; GPU buffers are reused through a small pool to reduce allocation churn.
 */
public final class CubimisedTerrainRenderer implements AutoCloseable {
    private final Map<Long, ChunkGpuData> chunks = new HashMap<>();
    private final ArrayDeque<CubimisedGpuBuffer> pool = new ArrayDeque<>();
    private long uploadedBytes;
    private long drawCalls;
    private long visibleChunks;
    private long culledChunks;
    private final java.util.Set<Long> coveredChunks = new java.util.HashSet<>();

    public void replaceChunk(ChunkPos pos, CubimisedChunkRenderData data) {
        if (pos == null || data == null) return;
        ChunkGpuData old = chunks.remove(pos.toLong());
        if (old != null) {
            old.close(pool);
        }

        ChunkGpuData gpu = new ChunkGpuData(data.origin());
        for (Map.Entry<RenderLayer, net.minecraft.client.render.BufferBuilder.BuiltBuffer> entry : data.meshes().entrySet()) {
            CubimisedGpuBuffer buffer = pool.pollFirst();
            if (buffer == null) buffer = new CubimisedGpuBuffer();
            buffer.upload(new CubimisedChunkMesh(entry.getValue()));
            if (buffer.isUploaded()) {
                gpu.buffers.put(entry.getKey(), buffer);
                uploadedBytes += entry.getValue().getVertexBuffer().remaining();
            } else {
                pool.offerFirst(buffer);
            }
        }
        data.release();
        chunks.put(pos.toLong(), gpu);
    }

    /** Legacy layer upload entry point. */
    public void upload(RenderLayer layer, net.minecraft.client.render.BufferBuilder.BuiltBuffer mesh) {
        if (layer == null || mesh == null || mesh.isEmpty()) return;
        CubimisedGpuBuffer buffer = pool.pollFirst();
        if (buffer == null) buffer = new CubimisedGpuBuffer();
        buffer.upload(new CubimisedChunkMesh(mesh));
        pool.offerLast(buffer);
    }

    /** Draws Cubimised buffers while the vanilla RenderLayer state/shader is active. */
    public boolean renderLayer(RenderLayer layer, double cameraX, double cameraY, double cameraZ) {
        if (layer == null || chunks.isEmpty()) return false;
        ArrayList<ChunkGpuData> visible = new ArrayList<>();
        double maxDistance = 512.0;
        double maxSq = maxDistance * maxDistance;
        for (ChunkGpuData chunk : chunks.values()) {
            CubimisedGpuBuffer buffer = chunk.buffers.get(layer);
            if (buffer == null || !buffer.isUploaded()) continue;
            double dx = chunk.origin.getX() + 8.0 - cameraX;
            double dy = chunk.origin.getY() + 8.0 - cameraY;
            double dz = chunk.origin.getZ() + 8.0 - cameraZ;
            if (dx * dx + dy * dy + dz * dz <= maxSq) visible.add(chunk);
        }
        if (visible.isEmpty()) return false;

        // Translucent terrain must be submitted far-to-near.
        if (layer == RenderLayer.getTranslucent() || layer == RenderLayer.getTranslucentMovingBlock()) {
            visible.sort(Comparator.comparingDouble(
                    chunk -> -distanceSq(chunk.origin, cameraX, cameraY, cameraZ)));
        }

        layer.startDrawing();
        try {
            for (ChunkGpuData chunk : visible) {
                CubimisedGpuBuffer buffer = chunk.buffers.get(layer);
                if (buffer != null && buffer.isUploaded()) {
                    buffer.draw();
                    drawCalls++;
                }
            }
        } finally {
            layer.endDrawing();
        }
        visibleChunks = visible.size();
        return true;
    }

    private static double distanceSq(net.minecraft.util.math.BlockPos pos, double x, double y, double z) {
        double dx = pos.getX() + 8.0 - x;
        double dy = pos.getY() + 8.0 - y;
        double dz = pos.getZ() + 8.0 - z;
        return dx * dx + dy * dy + dz * dz;
    }

    public boolean hasCoverage(Camera camera, int radius) {
        if (camera == null) return false;
        int cx = ((int) Math.floor(camera.getPos().x)) >> 4;
        int cz = ((int) Math.floor(camera.getPos().z)) >> 4;
        int r = Math.max(1, radius);
        for (int z = cz - r; z <= cz + r; z++) {
            for (int x = cx - r; x <= cx + r; x++) {
                if (!coveredChunks.contains(new ChunkPos(x, z).toLong())) return false;
            }
        }
        return true;
    }

    public void submit(RenderLayer layer, Matrix4f view, Matrix4f projection, ShaderProgram shader) {
        if (layer == null) return;
        layer.startDrawing();
        try {
            for (ChunkGpuData chunk : chunks.values()) {
                CubimisedGpuBuffer buffer = chunk.buffers.get(layer);
                if (buffer == null || !buffer.isUploaded()) continue;
                buffer.draw(view, projection, shader);
                drawCalls++;
            }
            visibleChunks = chunks.size();
        } finally {
            layer.endDrawing();
        }
    }

    /** Replaces a single 16x16x16 section without touching neighboring sections. */
    public void replaceSection(ChunkPos pos, int sectionY, CubimisedChunkRenderData data) {
        if (pos == null || data == null) return;
        long key = sectionKey(pos.toLong(), sectionY);
        ChunkGpuData old = chunks.remove(key);
        if (old != null) old.close(pool);

        ChunkGpuData gpu = new ChunkGpuData(data.origin());
        for (Map.Entry<RenderLayer, net.minecraft.client.render.BufferBuilder.BuiltBuffer> entry : data.meshes().entrySet()) {
            CubimisedGpuBuffer buffer = pool.pollFirst();
            if (buffer == null) buffer = new CubimisedGpuBuffer();
            buffer.upload(new CubimisedChunkMesh(entry.getValue()));
            if (buffer.isUploaded()) {
                gpu.buffers.put(entry.getKey(), buffer);
                uploadedBytes += entry.getValue().getVertexBuffer().remaining();
            } else {
                pool.offerFirst(buffer);
            }
        }
        data.release();
        chunks.put(key, gpu);
        coveredChunks.add(pos.toLong());
    }

    private static long sectionKey(long chunkKey, int sectionY) {
        return chunkKey ^ (0x9E3779B97F4A7C15L * (long) sectionY);
    }

    public void clearChunk(ChunkPos pos) {
        if (pos == null) return;
        ChunkGpuData data = chunks.remove(pos.toLong());
        if (data != null) data.close(pool);
    }

    public int chunkCount() { return chunks.size(); }
    public long uploadedBytes() { return uploadedBytes; }
    public long drawCalls() { return drawCalls; }
    public long visibleChunks() { return visibleChunks; }
    public long culledChunks() { return culledChunks; }

    @Override public void close() {
        for (ChunkGpuData data : chunks.values()) data.close(pool);
        chunks.clear();
        coveredChunks.clear();
        while (!pool.isEmpty() pool.poll().close();
    }

    private static final class ChunkGpuData {
        final net.minecraft.util.math.BlockPos origin;
        final Map<RenderLayer, CubimisedGpuBuffer> buffers = new IdentityHashMap<>();

        ChunkGpuData(net.minecraft.util.math.BlockPos origin) {
            this.origin = origin.toImmutable();
        }

        void close(ArrayDeque<CubimisedGpuBuffer> pool) {
            for (CubimisedGpuBuffer buffer : buffers.values()) {
                if (buffer.isUploaded()) pool.offerLast(buffer);
                else buffer.close();
            }
            buffers.clear();
        }
    }
}

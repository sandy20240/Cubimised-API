package dev.cubimised.api.client.render;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.ShaderProgram;
import net.minecraft.util.math.ChunkPos;
import org.joml.Matrix4f;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

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
        while (!pool.isEmpty()) pool.poll().close();
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

package dev.cubimised.api.client.render;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexBufferLayout;
import java.util.EnumMap;
import java.util.Map;

/**
 * Terrain backend. Keeps one GPU buffer per terrain render layer and submits
 * completed meshes in batches. The vanilla chunk builder remains the fallback
 * until the custom block-state mesher is enabled.
 */
public final class CubimisedTerrainRenderer implements AutoCloseable {
    private final Map<RenderLayer, CubimisedGpuBuffer> buffers = new java.util.IdentityHashMap<>();
    private long uploadedBytes;
    private long drawCalls;

    public void upload(RenderLayer layer, BufferBuilder.BuiltBuffer mesh) {
        if (layer == null || mesh == null || mesh.isEmpty()) return;
        CubimisedGpuBuffer buffer = buffers.computeIfAbsent(layer, k -> new CubimisedGpuBuffer());
        CubimisedChunkMesh wrapped = new CubimisedChunkMesh(mesh);
        uploadedBytes += wrapped.vertexBytes();
        buffer.upload(wrapped);
    }

    public void submit(RenderLayer layer) {
        CubimisedGpuBuffer buffer = buffers.get(layer);
        if (buffer != null && buffer.isUploaded()) {
            buffer.draw();
            drawCalls++;
        }
    }

    public long uploadedBytes() { return uploadedBytes; }
    public long drawCalls() { return drawCalls; }

    @Override public void close() {
        for (CubimisedGpuBuffer buffer : buffers.values()) buffer.close();
        buffers.clear();
    }
}

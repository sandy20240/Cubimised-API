package dev.cubimised.api.client.render;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;

/**
 * Reusable GPU buffer wrapper. Uploads occur only on the render thread.
 */
public final class CubimisedGpuBuffer implements AutoCloseable {
    private final VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
    private boolean uploaded;

    public void upload(CubimisedChunkMesh mesh) {
        if (mesh == null || mesh.isEmpty()) {
            uploaded = false;
            return;
        }
        buffer.bind();
        buffer.upload(mesh.builtBuffer());
        VertexBuffer.unbind();
        uploaded = true;
    }

    public void draw() {
        if (uploaded) buffer.draw();
    }

    public boolean isUploaded() { return uploaded; }

    @Override public void close() {
        buffer.close();
        uploaded = false;
    }
}

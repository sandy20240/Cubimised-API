package dev.cubimised.api.client.render;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.gl.ShaderProgram;
import org.joml.Matrix4f;

/** Reusable render-thread GPU vertex buffer. */
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
        if (!uploaded) return;
        buffer.bind();
        buffer.draw();
        VertexBuffer.unbind();
    }

    public void draw(Matrix4f view, Matrix4f projection, ShaderProgram shader) {
        if (!uploaded || shader == null) return;
        buffer.bind();
        buffer.draw(view, projection, shader);
        VertexBuffer.unbind();
    }

    public boolean isUploaded() { return uploaded; }

    @Override public void close() {
        buffer.close();
        uploaded = false;
    }
}

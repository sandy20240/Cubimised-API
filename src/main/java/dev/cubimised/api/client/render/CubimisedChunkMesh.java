package dev.cubimised.api.client.render;

import net.minecraft.client.render.BufferBuilder;

/** Immutable CPU-side mesh produced by the chunk meshing stage. */
public final class CubimisedChunkMesh {
    private final BufferBuilder.BuiltBuffer builtBuffer;
    private final long vertexBytes;

    public CubimisedChunkMesh(BufferBuilder.BuiltBuffer builtBuffer) {
        this.builtBuffer = builtBuffer;
        this.vertexBytes = builtBuffer == null ? 0L : builtBuffer.getVertexBuffer().remaining();
    }

    public BufferBuilder.BuiltBuffer builtBuffer() { return builtBuffer; }
    public long vertexBytes() { return vertexBytes; }
    public boolean isEmpty() { return builtBuffer == null || builtBuffer.isEmpty(); }
}

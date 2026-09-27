package dev.cubimised.api.client.renderer.chunk;

import java.util.Arrays;

/**
 * Immutable CPU-side mesh payload produced by chunk workers.
 *
 * <p>Vertex layout is deliberately backend-neutral: the renderer owns interpretation
 * of the packed attributes and GPU upload. Coordinates are relative to the chunk
 * section origin.</p>
 */
public final class ChunkMeshData {
    private final int sectionX;
    private final int sectionY;
    private final int sectionZ;
    private final float[] vertices;
    private final int vertexStride;
    private final int vertexCount;

    public ChunkMeshData(int sectionX, int sectionY, int sectionZ,
                         float[] vertices, int vertexStride) {
        if (vertices == null) throw new IllegalArgumentException("vertices cannot be null");
        if (vertexStride <= 0) throw new IllegalArgumentException("vertexStride must be positive");
        if (vertices.length % vertexStride != 0) {
            throw new IllegalArgumentException("Vertex array length must be divisible by stride");
        }
        this.sectionX = sectionX;
        this.sectionY = sectionY;
        this.sectionZ = sectionZ;
        this.vertices = Arrays.copyOf(vertices, vertices.length);
        this.vertexStride = vertexStride;
        this.vertexCount = vertices.length / vertexStride;
    }

    public int sectionX() { return sectionX; }
    public int sectionY() { return sectionY; }
    public int sectionZ() { return sectionZ; }
    public int vertexStride() { return vertexStride; }
    public int vertexCount() { return vertexCount; }
    public int floatCount() { return vertices.length; }

    /** Returns a defensive copy; callers cannot mutate a queued mesh. */
    public float[] copyVertices() {
        return Arrays.copyOf(vertices, vertices.length);
    }

    public long estimatedBytes() {
        return (long) vertices.length * Float.BYTES;
    }
}

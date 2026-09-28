package dev.cubimised.api.client.render;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.BlockPos;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/** CPU-side section/chunk mesh split by Minecraft terrain render layer. */
public final class CubimisedChunkRenderData {
    private final BlockPos origin;
    private final Map<RenderLayer, BufferBuilder.BuiltBuffer> meshes;

    public CubimisedChunkRenderData(BlockPos origin, Map<RenderLayer, BufferBuilder.BuiltBuffer> meshes) {
        this.origin = origin.toImmutable();
        this.meshes = Collections.unmodifiableMap(new IdentityHashMap<>(meshes));
    }

    public BlockPos origin() { return origin; }
    public Map<RenderLayer, BufferBuilder.BuiltBuffer> meshes() { return meshes; }
    public boolean isEmpty() { return meshes.isEmpty(); }

    public void release() {
        for (BufferBuilder.BuiltBuffer mesh : meshes.values()) {
            if (mesh != null) mesh.release();
        }
    }
}

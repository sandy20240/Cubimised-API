package dev.cubimised.api.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * First real Cubimised terrain mesher.
 *
 * It reuses Minecraft's baked block models and lighting rules but performs
 * section traversal and layer batching itself. This is intentionally separate
 * from ChunkBuilder so it can later replace its task/upload path.
 */
public final class CubimisedChunkMesher {
    private CubimisedChunkMesher() {}

    public static CubimisedChunkRenderData mesh(ChunkRendererRegion region, BlockPos origin, int minY, int maxY) {
        MinecraftClient client = MinecraftClient.getInstance();
        BlockRenderManager renderer = client.getBlockRenderManager();
        Map<RenderLayer, BufferBuilder> builders = new IdentityHashMap<>();
        MatrixStack matrices = new MatrixStack();

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int y = minY; y <= maxY; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    pos.set(origin.getX() + x, y, origin.getZ() + z);
                    BlockState state = region.getBlockState(pos);
                    if (state.isAir()) continue;

                    RenderLayer layer = RenderLayers.getBlockLayer(state);
                    BufferBuilder builder = builders.computeIfAbsent(layer,
                            l -> new BufferBuilder(Math.max(256, l.getExpectedBufferSize())));

                    if (!builder.isBuilding()) {
                        builder.begin(layer.getDrawMode(), layer.getVertexFormat());
                    }

                    matrices.push();
                    matrices.translate(-origin.getX(), -origin.getY(), -origin.getZ());
                    renderer.renderBlock(state, pos, region, matrices, builder, true,
                            Random.create(state.getRenderingSeed(pos)));
                    matrices.pop();
                }
            }
        }

        Map<RenderLayer, BufferBuilder.BuiltBuffer> finished = new IdentityHashMap<>();
        for (Map.Entry<RenderLayer, BufferBuilder> entry : builders.entrySet()) {
            BufferBuilder.BuiltBuffer buffer = entry.getValue().endNullable();
            if (buffer != null && !buffer.isEmpty()) {
                finished.put(entry.getKey(), buffer);
            } else if (buffer != null) {
                buffer.release();
            }
        }

        return new CubimisedChunkRenderData(origin, finished);
    }
}

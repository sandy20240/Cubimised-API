package dev.cubimised.api.mixin;

import dev.cubimised.api.CubimisedApi;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Keeps Minecraft's built-in chunk-graph culling enabled while Android Turbo is active.
 *
 * <p>The 1.20.1 renderer already has a chunk-culling traversal. Cubimised does not
 * replace that traversal here; this hook makes sure the expensive fallback path is
 * not selected while the low-end adaptive renderer policy is active.</p>
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererCullingMixin {
    @ModifyArg(
            method = "setupTerrain",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/WorldRenderer;collectRenderableChunks(Ljava/util/LinkedHashSet;Lnet/minecraft/client/render/WorldRenderer$ChunkInfoList;Lnet/minecraft/util/math/Vec3d;Ljava/util/Queue;Z)V"
            ),
            index = 4
    )
    private boolean cubimised$forceChunkCulling(boolean vanillaEnabled) {
        return CubimisedApi.androidTurboEnabled || vanillaEnabled;
    }
}

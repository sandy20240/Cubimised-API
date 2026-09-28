package dev.cubimised.api.mixin;

import dev.cubimised.api.client.render.CubimisedRenderCore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks the 1.20.1 world-render lifecycle into Cubimised's render core.
 * Vanilla remains the fallback backend while the Cubimised scheduling layer
 * progressively takes over render preparation.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void cubimised$beginRender(
            MatrixStack matrices,
            float tickDelta,
            long limitTime,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightmapTextureManager lightmapTextureManager,
            Matrix4f projectionMatrix,
            CallbackInfo ci) {
        CubimisedRenderCore.beginFrame(MinecraftClient.getInstance(), camera);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void cubimised$endRender(
            MatrixStack matrices,
            float tickDelta,
            long limitTime,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightmapTextureManager lightmapTextureManager,
            Matrix4f projectionMatrix,
            CallbackInfo ci) {
        CubimisedRenderCore.endFrame();
    }

    @Inject(method = "updateChunks", at = @At("HEAD"))
    private void cubimised$prepareChunkWork(Camera camera, CallbackInfo ci) {
        CubimisedRenderCore.scheduler().prepareChunkPass(camera);
    }
}

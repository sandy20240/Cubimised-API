package dev.cubimised.api.mixin;

import dev.cubimised.api.client.render.CubimisedRenderCore;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Integrates the Cubimised render scheduler with Minecraft's 1.20.1 world
 * renderer. This is intentionally a cooperative backend: vanilla remains the
 * rendering fallback while Cubimised takes ownership of scheduling/telemetry.
 */
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void cubimised$beginRender(
            Matrix4f positionMatrix,
            Matrix4f projectionMatrix,
            float tickDelta,
            long limitTime,
            boolean renderBlockOutline,
            Camera camera,
            net.minecraft.client.render.GameRenderer gameRenderer,
            net.minecraft.client.render.LightmapTextureManager lightmapTextureManager,
            net.minecraft.client.util.math.MatrixStack matrices,
            CallbackInfo ci) {
        CubimisedRenderCore.beginFrame(
                net.minecraft.client.MinecraftClient.getInstance(), camera);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void cubimised$endRender(
            Matrix4f positionMatrix,
            Matrix4f projectionMatrix,
            float tickDelta,
            long limitTime,
            boolean renderBlockOutline,
            Camera camera,
            net.minecraft.client.render.GameRenderer gameRenderer,
            net.minecraft.client.render.LightmapTextureManager lightmapTextureManager,
            net.minecraft.client.util.math.MatrixStack matrices,
            CallbackInfo ci) {
        CubimisedRenderCore.endFrame();
    }

    @Inject(method = "updateChunks", at = @At("HEAD"))
    private void cubimised$prepareChunkWork(Camera camera, CallbackInfo ci) {
        CubimisedRenderCore.scheduler().prepareChunkPass(camera);
    }
}

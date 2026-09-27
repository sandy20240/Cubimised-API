package dev.cubimised.api.mixin;

import dev.cubimised.api.client.renderer.horizon.HorizonRenderer;
import dev.cubimised.api.CubimisedApi;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.LightmapTextureManager;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererHorizonMixin {
    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void cubimised$renderHorizon(
            MatrixStack matrices,
            float tickDelta,
            long limitTime,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightmapTextureManager lightmapTextureManager,
            Matrix4f positionMatrix,
            CallbackInfo ci) {
        if (CubimisedApi.builtInHorizonsEnabled) {
            HorizonRenderer.render(matrices, camera);
        }
    }
}
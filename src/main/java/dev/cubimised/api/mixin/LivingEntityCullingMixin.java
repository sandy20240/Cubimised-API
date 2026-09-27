package dev.cubimised.api.mixin;

import dev.cubimised.api.CubimisedApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Basic distance culling for living-entity model rendering.
 * Does not remove entities from the world or affect server simulation.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityCullingMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cubimised$cullDistantLivingEntity(
            LivingEntity entity,
            float entityYaw,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!CubimisedApi.entityCullingEnabled || client.player == null || entity == client.player) {
            return;
        }

        double distance = CubimisedApi.cullingDistanceBlocks * (CubimisedApi.entityDensity / 100.0);
        if (entity.squaredDistanceTo(client.player) > distance * distance) {
            ci.cancel();
        }
    }
}

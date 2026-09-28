package dev.cubimised.api.mixin;

import dev.cubimised.api.CubimisedApi;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public abstract class ParticleReductionMixin {
    @Inject(method = "addParticle", at = @At("HEAD"), cancellable = true)
    private void cubimised$reduceParticleCount(
            ParticleEffect parameters,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ,
            CallbackInfoReturnable<Particle> cir
    ) {
        if ((CubimisedApi.reduceParticles || CubimisedApi.autoReduceParticles) && (System.nanoTime() & 3L) != 0L) {
            cir.setReturnValue(null);
        }
    }
}
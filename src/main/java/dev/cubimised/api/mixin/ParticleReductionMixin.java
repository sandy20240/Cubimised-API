package dev.cubimised.api.mixin;

import dev.cubimised.api.CubimisedApi;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public abstract class ParticleReductionMixin {
    @Inject(method = "addParticle", at = @At("HEAD"), cancellable = true)
    private void cubimised$reduceParticleCount(Particle particle, CallbackInfo ci) {
        if (CubimisedApi.reduceParticles && particle != null && (System.nanoTime() & 3L) != 0L) ci.cancel();
    }
}
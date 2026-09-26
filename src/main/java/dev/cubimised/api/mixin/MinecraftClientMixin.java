package dev.cubimised.api.mixin;

import dev.cubimised.api.client.CubimisedClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void cubimised$recordFrame(boolean tick, CallbackInfo ci) {
        CubimisedClient.recordFrame();
    }
}

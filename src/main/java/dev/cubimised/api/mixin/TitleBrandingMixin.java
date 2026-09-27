package dev.cubimised.api.mixin;

import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleBrandingMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void cubimised$titleBranding(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        TitleScreen screen = (TitleScreen)(Object)this;
        context.drawTextWithShadow(net.minecraft.client.MinecraftClient.getInstance().textRenderer, Text.literal("Cubimised API • World MTR"), 6, screen.height - 14, 0xA0FFFFFF);
    }
}
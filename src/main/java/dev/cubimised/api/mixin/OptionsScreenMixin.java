package dev.cubimised.api.mixin;

import dev.cubimised.api.client.PerformanceScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    protected OptionsScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void cubimised$addVideoSettingsEntry(CallbackInfo ci) {
        addDrawableChild(ButtonWidget.builder(Text.literal("Cubimised Video"), button ->
                client.setScreen(new PerformanceScreen((Screen)(Object)this)))
            .dimensions(width - 148, 8, 140, 20).build());
    }
}
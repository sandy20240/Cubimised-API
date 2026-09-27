package dev.cubimised.api.mixin;

import dev.cubimised.api.client.PerformanceScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends Screen {
    @Shadow protected int x;
    @Shadow protected int y;

    protected InventoryScreenMixin(Text title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void cubimised$addPanelButton(CallbackInfo ci) {
        int buttonX = Math.min(width - 82, x + 176 + 6);
        int buttonY = Math.max(6, y + 5);
        addDrawableChild(ButtonWidget.builder(Text.literal("CUBI"), button ->
                client.setScreen(new PerformanceScreen((Screen)(Object)this)))
            .dimensions(buttonX, buttonY, 48, 20).build());
    }
}
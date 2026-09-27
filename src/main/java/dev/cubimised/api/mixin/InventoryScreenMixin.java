package dev.cubimised.api.mixin;

import dev.cubimised.api.client.PerformanceScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the Cubimised settings shortcut without shadowing HandledScreen internals.
 * This keeps the mixin stable against the 1.20.1 mapped field names.
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends Screen {
    protected InventoryScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void cubimised$addPanelButton(CallbackInfo ci) {
        int buttonX = Math.max(4, width - 56);
        int buttonY = 6;

        addDrawableChild(ButtonWidget.builder(Text.literal("CUBI"), button ->
                client.setScreen(new PerformanceScreen((Screen) (Object) this)))
            .dimensions(buttonX, buttonY, 48, 20)
            .build());
    }
}

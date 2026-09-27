package dev.cubimised.api.client;

import dev.cubimised.api.CubimisedApi;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** In-game controls for Cubimised's client-side beta options. */
public final class PerformanceScreen extends Screen {
    private final Screen parent;
    public PerformanceScreen(Screen parent) {
        super(Text.literal("Cubimised Performance"));
        this.parent = parent;
    }

    @Override protected void init() {
        int x = this.width / 2 - 105;
        int y = this.height / 4;
        addToggle("FPS Counter", () -> CubimisedApi.showFps, v -> CubimisedApi.showFps = v, x, y);
        addToggle("Entity Culling", () -> CubimisedApi.entityCullingEnabled, v -> CubimisedApi.entityCullingEnabled = v, x, y + 25);
        addToggle("Block Entity Culling", () -> CubimisedApi.blockEntityCullingEnabled, v -> CubimisedApi.blockEntityCullingEnabled = v, x, y + 50);
        addToggle("Reduced Particles", () -> CubimisedApi.reduceParticles, v -> CubimisedApi.reduceParticles = v, x, y + 75);
        addToggle("Dynamic Resolution (experimental)", () -> CubimisedApi.dynamicResolutionEnabled, v -> CubimisedApi.dynamicResolutionEnabled = v, x, y + 100);
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close()).dimensions(x, y + 135, 210, 20).build());
    }

    private void addToggle(String label, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter, int x, int y) {
        ButtonWidget button = ButtonWidget.builder(toggleText(label, getter.getAsBoolean()), b -> {
            boolean next = !getter.getAsBoolean();
            setter.accept(next);
            b.setMessage(toggleText(label, next));
        }).dimensions(x, y, 210, 20).build();
        addDrawableChild(button);
    }

    private Text toggleText(String label, boolean enabled) {
        return Text.literal(label + ": " + (enabled ? "ON" : "OFF"));
    }

    @Override public void close() { if (client != null) client.setScreen(parent); }
}

package dev.cubimised.api.client;

import dev.cubimised.api.CubimisedApi;
import net.minecraft.client.MinecraftClient;

/**
 * Lightweight built-in distant-horizon controller.
 *
 * <p>This keeps far terrain intentionally coarse and CPU-cheap. It is designed
 * as the foundation for Cubimised's own horizon renderer, rather than loading
 * full-resolution Minecraft chunks hundreds of chunks away.</p>
 */
public final class HorizonLodManager {
    private int tickCounter;
    private int activeRadius = 0;
    private int activeStep = 0;

    public void tick(MinecraftClient client) {
        if (!CubimisedApi.builtInHorizonsEnabled || client.world == null || client.player == null) {
            activeRadius = 0;
            activeStep = 0;
            return;
        }

        if (++tickCounter < 20) return;
        tickCounter = 0;

        double frameMs = CubimisedApi.currentFrameMs;
        int radius = CubimisedApi.horizonRadiusChunks;
        int step = CubimisedApi.horizonLodStep;

        // Under pressure, widen the sampling step before reducing the horizon.
        if (CubimisedApi.androidTurboEnabled) {
            if (frameMs > 22.2) {
                step = Math.min(16, step + 4);
                radius = Math.min(radius, 64);
            } else if (frameMs > 16.7) {
                step = Math.min(12, step + 2);
                radius = Math.min(radius, 80);
            } else if (frameMs < 13.5) {
                step = Math.max(2, step - 1);
                radius = Math.min(256, radius + 16);
            }
        }

        activeRadius = Math.max(32, radius);
        activeStep = Math.max(2, step);
    }

    /** Number of chunks represented by the coarse horizon layer. */
    public int getActiveRadiusChunks() {
        return activeRadius;
    }

    /** Horizontal sampling step used by the future GPU horizon backend. */
    public int getActiveLodStep() {
        return activeStep;
    }

    public void reset() {
        tickCounter = 0;
        activeRadius = 0;
        activeStep = 0;
    }
}
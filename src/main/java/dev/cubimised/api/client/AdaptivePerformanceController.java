package dev.cubimised.api.client;

import dev.cubimised.api.CubimisedApi;
import net.minecraft.client.MinecraftClient;

public final class AdaptivePerformanceController {
    private int tickCounter;
    private int goodTicks;
    private int badTicks;
    private int appliedViewDistance = -1;

    public void tick(MinecraftClient client) {
        if (!CubimisedApi.androidTurboEnabled || client.world == null) return;
        if (++tickCounter < 10) return;
        tickCounter = 0;

        double frameMs = CubimisedApi.currentFrameMs;
        long used = CubimisedApi.usedMemoryMb;
        long max = Math.max(1L, CubimisedApi.maxMemoryMb);
        boolean memoryPressure = used > max * 0.82;

        if (frameMs > 22.2 || memoryPressure) {
            badTicks++;
            goodTicks = 0;
        } else if (frameMs < 13.5 && !memoryPressure) {
            goodTicks++;
            badTicks = 0;
        } else {
            goodTicks = Math.max(0, goodTicks - 1);
            badTicks = Math.max(0, badTicks - 1);
        }

        if (badTicks >= 2) {
            degrade(client);
            badTicks = 0;
        } else if (goodTicks >= 6) {
            recover(client);
            goodTicks = 0;
        }

        CubimisedApi.renderUploadBudget = frameMs > 20.0 ? 1 : (frameMs > 15.0 ? 2 : 4);
    }

    private void degrade(MinecraftClient client) {
        int current = client.options.getViewDistance().getValue();
        int next = Math.max(CubimisedApi.turboMinViewDistance, current - 1);
        next = Math.min(next, CubimisedApi.chunkViewDistanceCap);
        if (next != appliedViewDistance) {
            client.options.getViewDistance().setValue(next);
            appliedViewDistance = next;
        }
        CubimisedApi.entityDensity = Math.max(CubimisedApi.turboMinEntityDensity, CubimisedApi.entityDensity - 10);
        CubimisedApi.reduceParticles = true;
        CubimisedApi.updateCullingDistance(next * 16);
        if (CubimisedApi.dynamicResolutionEnabled) {
            CubimisedApi.renderScale = Math.max(CubimisedApi.minScale, CubimisedApi.renderScale - 0.04);
        }
    }

    private void recover(MinecraftClient client) {
        int current = client.options.getViewDistance().getValue();
        int next = Math.min(CubimisedApi.turboMaxViewDistance, current + 1);
        next = Math.min(next, CubimisedApi.chunkViewDistanceCap);
        if (next != appliedViewDistance) {
            client.options.getViewDistance().setValue(next);
            appliedViewDistance = next;
        }
        CubimisedApi.entityDensity = Math.min(CubimisedApi.turboMaxEntityDensity, CubimisedApi.entityDensity + 5);
        CubimisedApi.updateCullingDistance(next * 16);
        if (CubimisedApi.dynamicResolutionEnabled) {
            CubimisedApi.renderScale = Math.min(CubimisedApi.maxScale, CubimisedApi.renderScale + 0.015);
        }
    }

    public void reset() {
        tickCounter = 0;
        goodTicks = 0;
        badTicks = 0;
        appliedViewDistance = -1;
    }
}
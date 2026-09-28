package dev.cubimised.api.client.render;

import dev.cubimised.api.CubimisedApi;

/** Centralized renderer switches; defaults preserve correctness. */
public final class CubimisedRenderConfig {
    private CubimisedRenderConfig() {}

    public static boolean customTerrainEnabled() {
        return CubimisedApi.smartBoosterEnabled;
    }

    public static int rebuildBudget() {
        return CubimisedApi.smartBoosterEnabled ? 2 : 1;
    }

    public static int visibilityRadius() {
        return Math.max(2, Math.min(32, CubimisedApi.chunkViewDistanceCap));
    }
}

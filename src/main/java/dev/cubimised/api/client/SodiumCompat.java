package dev.cubimised.api.client;

import net.fabricmc.loader.api.FabricLoader;

/** Small runtime compatibility helper. Sodium is optional; Cubimised never hard-depends on it. */
public final class SodiumCompat {
    private SodiumCompat() {}

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded("sodium");
    }

    /** Returns a conservative target cap when Sodium is present, avoiding aggressive client-option changes. */
    public static int recommendedViewDistanceCap(int configuredCap) {
        if (!isLoaded()) return configuredCap;
        return Math.min(configuredCap, 16);
    }
}

package dev.cubimised.api.client.renderer;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Owns renderer selection and lifecycle.
 *
 * <p>Milestone 1 intentionally keeps vanilla active until a Cubimised backend has
 * working terrain, entity, and resource-management passes. This avoids selecting an
 * incomplete renderer and leaving the player with a blank or broken world.</p>
 */
public final class RendererManager {
    private static final RendererManager INSTANCE = new RendererManager();

    private volatile RendererStatus status = RendererStatus.VANILLA_FALLBACK;
    private volatile String activeRendererId = "minecraft";
    private volatile String lastError = "";

    private RendererManager() {}

    public static RendererManager getInstance() {
        return INSTANCE;
    }

    /** Start renderer discovery. Must be called from the client initialization path. */
    public void initialize() {
        // Sodium is not a dependency. Detection is diagnostic only at this stage;
        // we do not crash, disable it, or claim Cubimised is rendering the world yet.
        boolean sodiumPresent = FabricLoader.getInstance().isModLoaded("sodium");
        activeRendererId = "minecraft";
        status = RendererStatus.VANILLA_FALLBACK;
        lastError = sodiumPresent
                ? "Sodium detected; Cubimised backend is not active yet. Vanilla/Sodium owns rendering."
                : "Cubimised backend is not active yet. Vanilla owns rendering.";
    }

    public RendererStatus getStatus() {
        return status;
    }

    public String getActiveRendererId() {
        return activeRendererId;
    }

    public String getLastError() {
        return lastError;
    }

    public boolean isCubimisedPrimary() {
        return status == RendererStatus.ACTIVE && "cubimised".equals(activeRendererId);
    }
}

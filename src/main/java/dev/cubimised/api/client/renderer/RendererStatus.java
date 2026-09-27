package dev.cubimised.api.client.renderer;

/** Current renderer lifecycle state, suitable for diagnostics and UI. */
public enum RendererStatus {
    VANILLA_FALLBACK,
    INITIALIZING,
    ACTIVE,
    FAILED,
    SHUTTING_DOWN
}

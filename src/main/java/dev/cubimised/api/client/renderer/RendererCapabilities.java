package dev.cubimised.api.client.renderer;

/** Immutable feature description for a renderer backend. */
public record RendererCapabilities(
        boolean terrain,
        boolean entities,
        boolean blockEntities,
        boolean particles,
        boolean shaderPipeline,
        boolean asynchronousChunkBuilds
) {
    /** Capabilities currently provided by Minecraft's built-in renderer. */
    public static RendererCapabilities vanillaFallback() {
        return new RendererCapabilities(true, true, true, true, true, false);
    }

    /** Capabilities planned for the Cubimised renderer as its passes are implemented. */
    public static RendererCapabilities cubimisedTarget() {
        return new RendererCapabilities(true, true, true, true, true, true);
    }
}

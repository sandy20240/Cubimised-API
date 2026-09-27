package dev.cubimised.api.client.renderer;

/**
 * Contract for Cubimised's renderer backends.
 *
 * <p>The first renderer milestone establishes the backend boundary while Minecraft's
 * vanilla renderer remains the active fallback. A future backend can implement terrain,
 * entity, and post-processing passes without coupling those systems to the config UI.</p>
 */
public interface CubimisedRenderer {
    /** Stable backend identifier shown in diagnostics. */
    String id();

    /** Human-readable renderer name. */
    String displayName();

    /** Initialize GPU resources on the render thread. */
    void initialize();

    /** Release all GPU resources on the render thread. */
    void shutdown();

    /** True after successful initialization. */
    boolean isInitialized();

    /** Capability flags supported by this backend. */
    RendererCapabilities capabilities();
}

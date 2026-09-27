package dev.cubimised.api.client;

import dev.cubimised.api.CubimisedApi;
import dev.cubimised.api.client.renderer.RendererManager;
import dev.cubimised.api.client.renderer.chunk.ChunkRendererPipeline;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

/** Client-side frame-time controller, preference sync, welcome screen, and beta HUD/settings. */
public final class CubimisedClient implements ClientModInitializer {
    private static long lastFrameNanos;
    private static double smoothedFrameMs = 1000.0 / 60.0;
    private static int ticks;
    private static int turboTick;
    private static int turboViewDistance = -1;
    private static KeyBinding openSettingsKey;
    private static ChunkRendererPipeline chunkPipeline;
    private static net.minecraft.client.world.ClientWorld lastWorld;
    private static boolean welcomeSeen = Files.exists(FabricLoader.getInstance().getConfigDir().resolve("cubimised-api-welcome.txt"));

    public static void markWelcomeSeen() {
        welcomeSeen = true;
        Path file = FabricLoader.getInstance().getConfigDir().resolve("cubimised-api-welcome.txt");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, "seen");
        } catch (IOException e) {
            System.err.println("[Cubimised API] Could not save welcome preference: " + e.getMessage());
        }
    }

    @Override public void onInitializeClient() {
        CubimisedConfig.load();
        RendererManager.getInstance().initialize();
        MinecraftClient minecraft = MinecraftClient.getInstance();
        int workers = Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() - 1));
        chunkPipeline = new ChunkRendererPipeline(workers, 128, minecraft::execute);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (chunkPipeline != null) {
                chunkPipeline.close();
                chunkPipeline = null;
            }
            lastWorld = null;
        });
        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cubimised_api.performance_settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O,
                "category.cubimised_api"));
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (CubimisedApi.showFps && client != null) {
                drawContext.drawTextWithShadow(client.textRenderer,
                        "Cubimised | FPS: " + client.getCurrentFps() + " | " + String.format(java.util.Locale.ROOT, "%.1f ms", CubimisedApi.currentFrameMs), 6, 6, 0x55FF55);
                drawContext.drawTextWithShadow(client.textRenderer, "Memory: " + CubimisedApi.usedMemoryMb + " / " + CubimisedApi.maxMemoryMb + " MB", 6, 18, 0xFFFFFF);
                drawContext.drawTextWithShadow(client.textRenderer, "Entities: " + (client.world == null ? 0 : client.world.getRegularEntityCount()), 6, 30, 0xFFFFFF);
                drawContext.drawTextWithShadow(client.textRenderer, "Renderer: " + RendererManager.getInstance().getActiveRendererId() + " (" + RendererManager.getInstance().getStatus().name() + ")", 6, 42, 0xAAAAFF);
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (chunkPipeline != null) {
                if (lastWorld != client.world) {
                    chunkPipeline.onWorldChanged();
                    lastWorld = client.world;
                }
                // Keep resource handoff bounded; a future GPU backend will enqueue
                // uploads here after generating actual section meshes.
                chunkPipeline.processUploads(2);
            }
            while (openSettingsKey.wasPressed()) {
                client.setScreen(new PerformanceScreen(client.currentScreen));
            }
            if (!welcomeSeen && client.currentScreen instanceof TitleScreen) {
                client.setScreen(new WelcomeScreen());
                return;
            }
            if (CubimisedApi.androidTurboEnabled) {
                applyAndroidTurbo(client);
            }
            if (CubimisedApi.smartBoosterEnabled && client.options.getViewDistance().getValue() > SodiumCompat.recommendedViewDistanceCap(CubimisedApi.chunkViewDistanceCap)) {
                client.options.getViewDistance().setValue(SodiumCompat.recommendedViewDistanceCap(CubimisedApi.chunkViewDistanceCap));
            }
            if (client.player == null || client.world == null) return;
            if (++ticks % 40 == 0) {
                var packet = PacketByteBufs.create();
                packet.writeVarInt(CubimisedApi.cullingDistanceBlocks);
                ClientPlayNetworking.send(CubimisedApi.CULLING_PREFERENCE_PACKET, packet);
            }
        });
    }

    private static void applyAndroidTurbo(MinecraftClient client) {
        if (++turboTick % 20 != 0) return;
        double frame = CubimisedApi.currentFrameMs;
        int desired;
        if (frame > 1000.0 / 45.0) desired = CubimisedApi.turboMinViewDistance;
        else if (frame > 1000.0 / 60.0) desired = Math.max(CubimisedApi.turboMinViewDistance, 8);
        else desired = CubimisedApi.turboMaxViewDistance;
        desired = Math.min(desired, CubimisedApi.chunkViewDistanceCap);
        if (turboViewDistance != desired) {
            client.options.getViewDistance().setValue(desired);
            turboViewDistance = desired;
        }
        CubimisedApi.entityDensity = frame > 1000.0 / 50.0 ? CubimisedApi.turboMinEntityDensity : CubimisedApi.turboMaxEntityDensity;
        CubimisedApi.reduceParticles = frame > 1000.0 / 55.0;
        CubimisedApi.entityCullingEnabled = true;
        CubimisedApi.blockEntityCullingEnabled = true;
        CubimisedApi.updateCullingDistance(desired * 16);
    }

    /** Called from the client render mixin once per frame. */
    public static void recordFrame() {
        long now = System.nanoTime();
        if (lastFrameNanos != 0L) {
            double frameMs = (now - lastFrameNanos) / 1_000_000.0;
            CubimisedApi.currentFrameMs = frameMs;
            Runtime runtime = Runtime.getRuntime();
            CubimisedApi.usedMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            CubimisedApi.maxMemoryMb = runtime.maxMemory() / (1024 * 1024);
            if (CubimisedApi.smartBoosterEnabled && frameMs > 1000.0 / Math.max(20, CubimisedApi.targetFps) * 1.3) {
                CubimisedApi.reduceParticles = true;
            }
            smoothedFrameMs = smoothedFrameMs * 0.9 + frameMs * 0.1;
            if (!CubimisedApi.dynamicResolutionEnabled) {
                CubimisedApi.renderScale = 1.0;
                lastFrameNanos = now;
                return;
            }
            double targetMs = 1000.0 / Math.max(1, CubimisedApi.targetFps);
            if (smoothedFrameMs > targetMs * 1.08) {
                CubimisedApi.renderScale = Math.max(CubimisedApi.minScale, CubimisedApi.renderScale - 0.025);
            } else if (smoothedFrameMs < targetMs * 0.88) {
                CubimisedApi.renderScale = Math.min(CubimisedApi.maxScale, CubimisedApi.renderScale + 0.01);
            }
        }
        lastFrameNanos = now;
    }

    public static double getRenderScale() {
        return Math.max(CubimisedApi.minScale, Math.min(CubimisedApi.maxScale, CubimisedApi.renderScale));
    }
}
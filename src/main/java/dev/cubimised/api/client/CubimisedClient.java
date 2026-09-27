package dev.cubimised.api.client;

import dev.cubimised.api.CubimisedApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.TitleScreen;\nimport net.minecraft.client.MinecraftClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

/** Client-side frame-time controller, preference sync, and first-launch welcome. */
public final class CubimisedClient implements ClientModInitializer {
    private static long lastFrameNanos;
    private static double smoothedFrameMs = 1000.0 / 60.0;
    private static int ticks;
    private static KeyBinding openSettingsKey;\n    private static boolean welcomeSeen = Files.exists(FabricLoader.getInstance().getConfigDir().resolve("cubimised-api-welcome.txt"));

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
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!welcomeSeen && client.currentScreen instanceof TitleScreen) {
                client.setScreen(new WelcomeScreen());
                return;
            }
            if (client.player == null || client.world == null) return;
            if (++ticks % 40 == 0) {
                var packet = PacketByteBufs.create();
                packet.writeVarInt(CubimisedApi.cullingDistanceBlocks);
                ClientPlayNetworking.send(CubimisedApi.CULLING_PREFERENCE_PACKET, packet);
            }
        });
    }

    /** Called from the client render mixin once per frame. */
    public static void recordFrame() {
        long now = System.nanoTime();
        if (lastFrameNanos != 0L) {
            double frameMs = (now - lastFrameNanos) / 1_000_000.0;
            smoothedFrameMs = smoothedFrameMs * 0.9 + frameMs * 0.1;
            if (!CubimisedApi.dynamicResolutionEnabled) { CubimisedApi.renderScale = 1.0; lastFrameNanos = now; return; }\n            double targetMs = 1000.0 / Math.max(1, CubimisedApi.targetFps);
            if (smoothedFrameMs > targetMs * 1.08) {
                CubimisedApi.renderScale = Math.max(CubimisedApi.minScale, CubimisedApi.renderScale - 0.025);
            } else if (smoothedFrameMs < targetMs * 0.88) {
                CubimisedApi.renderScale = Math.min(CubimisedApi.maxScale, CubimisedApi.renderScale + 0.01);
            }
        }
        lastFrameNanos = now;
    }

    private static final class MinecraftClientHolder { static MinecraftClient client() { return MinecraftClient.getInstance(); } }\n\n    public static double getRenderScale() {
        return Math.max(CubimisedApi.minScale, Math.min(CubimisedApi.maxScale, CubimisedApi.renderScale));
    }
}
package dev.cubimised.api.client;

import dev.cubimised.api.CubimisedApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

/** Client-side frame-time controller and preference sync. */
public final class CubimisedClient implements ClientModInitializer {
    private static long lastFrameNanos;
    private static double smoothedFrameMs = 1000.0 / 60.0;
    private static int ticks;

    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
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

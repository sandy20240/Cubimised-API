package dev.cubimised.api;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Common API. Uses Fabric's 1.20.1 PacketByteBuf networking API. */
public final class CubimisedApi implements ModInitializer {
    public static final String MOD_ID = "cubimised_api";
    public static final Identifier CULLING_PREFERENCE_PACKET = new Identifier(MOD_ID, "culling_preference");
    public static final Identifier SERVER_HANDSHAKE_PACKET = new Identifier(MOD_ID, "server_handshake");
    public static final Identifier CLIENT_HANDSHAKE_PACKET = new Identifier(MOD_ID, "client_handshake");
    public static final int NETWORK_PROTOCOL_VERSION = 1;
    private static final Map<UUID, Integer> PLAYER_DISTANCES = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> HANDSHAKE_DEADLINES = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> VERIFIED_CLIENTS = new ConcurrentHashMap<>();
    public static volatile double renderScale = 1.0;
    public static volatile double minScale = 0.5;
    public static volatile double maxScale = 1.0;
    public static volatile int targetFps = 60;
    public static volatile int cullingDistanceBlocks = 128;
    /** Client-side entity render distance culling toggle. */
    public static volatile boolean entityCullingEnabled = true;
    public static volatile boolean showFps = true;
    public static volatile boolean reduceParticles = false;
    public static volatile boolean blockEntityCullingEnabled = true;
    public static volatile boolean dynamicResolutionEnabled = false;
    public static volatile double maxCullDistSq = 128.0 * 128.0;
    public static volatile boolean smartBoosterEnabled = true;
    public static volatile int entityDensity = 100;
    public static volatile int chunkViewDistanceCap = 16;
    public static volatile String performanceProfile = "Balanced";
    /** Android/low-end adaptive mode: aggressively reduces CPU/GPU work when frame time rises. */
    public static volatile boolean androidTurboEnabled = false;
    public static volatile int turboMinViewDistance = 6;
    public static volatile int turboMaxViewDistance = 10;
    public static volatile int turboMinEntityDensity = 35;
    public static volatile int turboMaxEntityDensity = 70;
    public static volatile int adaptiveFrameTarget = 60;
    public static volatile double currentFrameMs = 16.67;
    public static volatile long usedMemoryMb = 0;
    public static volatile long maxMemoryMb = 0;
    /** Maximum number of render-thread resource uploads processed in one frame. */
    public static volatile int renderUploadBudget = 2;
    /** Built-in low-detail distant terrain/horizon system. */
    public static volatile boolean builtInHorizonsEnabled = true;
    /** Maximum distance, in chunks, covered by low-detail horizon data. */
    public static volatile int horizonRadiusChunks = 96;
    /** Horizon LOD detail: 1 = highest, larger values = cheaper/coarser. */
    public static volatile int horizonLodStep = 4;
    /** High-frequency performance monitor overlay, toggled with L. */
    public static volatile boolean performanceMonitorEnabled = false;
    /** Latest process CPU utilization percentage, or -1 when unavailable. */
    public static volatile double cpuUsagePercent = -1.0;
    /** Latest system CPU utilization percentage, or -1 when unavailable. */
    public static volatile double systemCpuUsagePercent = -1.0;
    /** Latest player latency in milliseconds, or -1 when unavailable. */
    public static volatile int pingMs = -1;
    /** Latest server TPS estimate, or -1 when unavailable client-side. */
    public static volatile double serverTps = -1.0;
    /** GPU telemetry is platform-specific and remains unavailable unless a backend provides it. */
    public static volatile double gpuUsagePercent = -1.0;
    public static volatile long gpuMemoryMb = -1L;
    public static volatile double gpuPowerWatts = -1.0;

    @Override public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(CLIENT_HANDSHAKE_PACKET, (server, player, handler, buf, responseSender) -> {
            int version = buf.readVarInt();
            server.execute(() -> {
                if (version == NETWORK_PROTOCOL_VERSION) {
                    VERIFIED_CLIENTS.put(player.getUuid(), Boolean.TRUE);
                    HANDSHAKE_DEADLINES.remove(player.getUuid());
                } else {
                    player.networkHandler.disconnect(Text.literal("Cubimised API protocol mismatch. Server requires protocol " + NETWORK_PROTOCOL_VERSION + "."));
                }
            });
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            UUID uuid = handler.player.getUuid();
            VERIFIED_CLIENTS.remove(uuid);
            HANDSHAKE_DEADLINES.put(uuid, System.nanoTime() + 5_000_000_000L);
            var packet = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
            packet.writeVarInt(NETWORK_PROTOCOL_VERSION);
            ServerPlayNetworking.send(handler.player, SERVER_HANDSHAKE_PACKET, packet);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID uuid = handler.player.getUuid();
            VERIFIED_CLIENTS.remove(uuid);
            HANDSHAKE_DEADLINES.remove(uuid);
            PLAYER_DISTANCES.remove(uuid);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = System.nanoTime();
            HANDSHAKE_DEADLINES.forEach((uuid, deadline) -> {
                if (now >= deadline && !VERIFIED_CLIENTS.containsKey(uuid)) {
                    ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
                    if (player != null) {
                        player.networkHandler.disconnect(Text.literal("Cubimised API is required on both the client and server."));
                    }
                    HANDSHAKE_DEADLINES.remove(uuid);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(CULLING_PREFERENCE_PACKET, (server, player, handler, buf, responseSender) -> {
            int requested = MathHelper.clamp(buf.readVarInt(), 16, 512);
            server.execute(() -> PLAYER_DISTANCES.put(player.getUuid(), requested));
        });
    }

    public static void updateCullingDistance(int distance) {
        cullingDistanceBlocks = MathHelper.clamp(distance, 16, 512);
        maxCullDistSq = (double) cullingDistanceBlocks * cullingDistanceBlocks;
    }

    public static void setPlayerCullingDistance(ServerPlayerEntity player, int distance) {
        PLAYER_DISTANCES.put(player.getUuid(), MathHelper.clamp(distance, 16, 512));
    }

    public static int getPlayerCullingDistance(ServerPlayerEntity player) {
        return PLAYER_DISTANCES.getOrDefault(player.getUuid(), cullingDistanceBlocks);
    }

    public static void clearPlayerPreference(ServerPlayerEntity player) {
        PLAYER_DISTANCES.remove(player.getUuid());
    }

    public static boolean shouldRenderOrSyncObject(Vec3d objectPos, Vec3d viewerPos) {
        return objectPos.squaredDistanceTo(viewerPos) <= maxCullDistSq;
    }

    public static boolean shouldRenderOrSyncObject(Vec3d objectPos, Vec3d viewerPos, int distanceBlocks) {
        double distance = MathHelper.clamp(distanceBlocks, 16, 512);
        return objectPos.squaredDistanceTo(viewerPos) <= distance * distance;
    }
}

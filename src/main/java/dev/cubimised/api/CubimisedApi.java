package dev.cubimised.api;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
    private static final Map<UUID, Integer> PLAYER_DISTANCES = new ConcurrentHashMap<>();
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

    @Override public void onInitialize() {
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

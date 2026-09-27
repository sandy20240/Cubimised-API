package dev.cubimised.api.client.renderer.horizon;

import dev.cubimised.api.CubimisedApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

/**
 * Built-in, dependency-free distant horizon renderer.
 *
 * <p>It samples the world surface at a coarse angular resolution and renders
 * a low-vertex silhouette. It deliberately does not load or rebuild distant
 * chunk meshes, which is the important property for low-end devices and heavy
 * modpacks.</p>
 */
public final class HorizonRenderer {
    private static final int MIN_SAMPLES = 64;
    private static final int MAX_SAMPLES = 256;

    private HorizonRenderer() {}

    public static void render(MatrixStack matrices, Camera camera) {
        if (!CubimisedApi.builtInHorizonsEnabled || camera == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null) return;

        int radiusChunks = CubimisedApi.horizonRadiusChunks;
        int radius = MathHelper.clamp(radiusChunks * 16, 512, 4096);
        int step = MathHelper.clamp(CubimisedApi.horizonLodStep, 2, 16);
        int samples = MathHelper.clamp((radius / Math.max(16, step * 4)), MIN_SAMPLES, MAX_SAMPLES);

        double cameraX = camera.getPos().x;
        double cameraY = camera.getPos().y;
        double cameraZ = camera.getPos().z;

        VertexConsumerProvider.Immediate consumers =
                client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertices = consumers.getBuffer(RenderLayer.getDebugQuads());

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        double angleStep = Math.PI * 2.0 / samples;

        for (int i = 0; i < samples; i++) {
            double a0 = i * angleStep;
            double a1 = (i + 1) * angleStep;

            double x0 = cameraX + Math.cos(a0) * radius;
            double z0 = cameraZ + Math.sin(a0) * radius;
            double x1 = cameraX + Math.cos(a1) * radius;
            double z1 = cameraZ + Math.sin(a1) * radius;

            int y0 = world.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE,
                    MathHelper.floor(x0), MathHelper.floor(z0));
            int y1 = world.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE,
                    MathHelper.floor(x1), MathHelper.floor(z1));

            // Keep the silhouette below the camera horizon and avoid enormous
            // vertical geometry on unusual dimensions.
            double top0 = MathHelper.clamp(y0, -64, 320);
            double top1 = MathHelper.clamp(y1, -64, 320);
            double bottom = Math.min(cameraY - 2.0, Math.min(top0, top1) - 4.0);

            int shade0 = terrainShade(world, y0);
            int shade1 = terrainShade(world, y1);

            vertex(vertices, matrix, x0 - cameraX, top0 - cameraY, z0 - cameraZ, shade0);
            vertex(vertices, matrix, x1 - cameraX, top1 - cameraY, z1 - cameraZ, shade1);
            vertex(vertices, matrix, x1 - cameraX, bottom - cameraY, z1 - cameraZ, shade1);
            vertex(vertices, matrix, x0 - cameraX, bottom - cameraY, z0 - cameraZ, shade0);
        }

        consumers.draw();
    }

    private static void vertex(VertexConsumer out, Matrix4f matrix,
                               double x, double y, double z, int shade) {
        out.vertex(matrix, (float) x, (float) y, (float) z)
                .color(shade, shade, shade, 255)
                .next();
    }

    private static int terrainShade(ClientWorld world, int y) {
        int sea = world.getSeaLevel();
        if (y < sea - 8) return 78;
        if (y < sea + 16) return 94;
        if (y < sea + 48) return 112;
        if (y < sea + 96) return 128;
        return 145;
    }
}
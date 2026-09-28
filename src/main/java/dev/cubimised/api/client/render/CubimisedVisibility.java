package dev.cubimised.api.client.render;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;

/** Allocation-free-ish chunk visibility helper. */
public final class CubimisedVisibility {
    private CubimisedVisibility() {}

    public static boolean visible(Frustum frustum, ChunkPos pos, int minY, int maxY) {
        if (frustum == null || pos == null) return true;
        Box box = new Box(pos.getStartX(), minY, pos.getStartZ(),
                pos.getEndX() + 1, maxY + 1, pos.getEndZ() + 1);
        return frustum.isVisible(box);
    }

    public static double distanceSq(Camera camera, ChunkPos pos) {
        double x = pos.getCenterX() - camera.getPos().x;
        double z = pos.getCenterZ() - camera.getPos().z;
        return x * x + z * z;
    }
}

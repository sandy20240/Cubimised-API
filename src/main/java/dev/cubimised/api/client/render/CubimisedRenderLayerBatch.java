package dev.cubimised.api.client.render;

import net.minecraft.client.render.RenderLayer;
import java.util.IdentityHashMap;
import java.util.Map;

/** Groups GPU buffers by RenderLayer to minimize state changes and submissions. */
public final class CubimisedRenderLayerBatch {
    private final Map<RenderLayer, Integer> counts = new IdentityHashMap<>();

    public void add(RenderLayer layer) {
        if (layer != null) counts.merge(layer, 1, Integer::sum);
    }

    public int count(RenderLayer layer) {
        return counts.getOrDefault(layer, 0);
    }

    public void clear() { counts.clear(); }
    public Map<RenderLayer, Integer> snapshot() { return new IdentityHashMap<>(counts); }
}

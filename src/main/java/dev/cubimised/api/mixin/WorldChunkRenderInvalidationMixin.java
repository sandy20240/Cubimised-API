package dev.cubimised.api.mixin;

import dev.cubimised.api.client.render.CubimisedRenderCore;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Invalidates Cubimised terrain after client-side chunk block changes.
 * Server simulation is untouched.
 */
@Mixin(WorldChunk.class)
public abstract class WorldChunkRenderInvalidationMixin {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void cubimised$invalidateSection(BlockPos pos, BlockState state, boolean moved,
                                             CallbackInfoReturnable<BlockState> cir) {
        CubimisedRenderCore.markDirty(pos);
    }
}

package kr.nexa.sablestepfix.mixin;

import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback.CollisionResult;
import dev.ryanhcode.sable.physics.callback.FragileBlockCallback;
import kr.nexa.sablestepfix.DeferredFragileHits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = FragileBlockCallback.class, remap = false)
public abstract class FragileBlockCallbackMixin {
    @Redirect(
            method = "sable$onCollision",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ryanhcode/sable/physics/callback/FragileBlockCallback;onHit(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lorg/joml/Vector3d;)Ldev/ryanhcode/sable/api/physics/callback/BlockSubLevelCollisionCallback$CollisionResult;"))
    private CollisionResult nexa$deferOnHitDuringStep(
            final FragileBlockCallback self,
            final ServerLevel level,
            final BlockPos pos,
            final BlockState state,
            final Vector3d hitPos) {
        return DeferredFragileHits.onHit(self, level, pos, state, hitPos);
    }
}

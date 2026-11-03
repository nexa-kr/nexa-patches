package kr.nexa.sablestepfix.mixin;

import dev.ryanhcode.sable.ActiveSableCompanion;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import kr.nexa.sablestepfix.RemovedBodyVelocity;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 이미 제거된 강체의 속도를 물으면 예외 대신 0 을 돌려준다. 이유는 {@link RemovedBodyVelocity}.
 *
 * <p>대상은 {@code ActiveSableCompanion.getVelocity(Level, SubLevelAccess, Vector3dc, Vector3d)} 안의 두 호출뿐이다
 * (클래스 전체에서 {@code RigidBodyHandle} 속도 호출은 이 두 개). {@code RigidBodyHandle} 자체는 건드리지 않는다.
 */
@Mixin(value = ActiveSableCompanion.class, remap = false)
public abstract class ActiveSableCompanionMixin {
    private static final String GET_VELOCITY =
            "getVelocity(Lnet/minecraft/world/level/Level;Ldev/ryanhcode/sable/companion/SubLevelAccess;Lorg/joml/Vector3dc;Lorg/joml/Vector3d;)Lorg/joml/Vector3d;";

    @Redirect(
            method = GET_VELOCITY,
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ryanhcode/sable/api/physics/handle/RigidBodyHandle;getLinearVelocity(Lorg/joml/Vector3d;)Lorg/joml/Vector3d;"))
    private Vector3d nexa$linearVelocityOrZero(final RigidBodyHandle handle, final Vector3d dest) {
        return handle.isValid() ? handle.getLinearVelocity(dest) : RemovedBodyVelocity.zero(dest);
    }

    @Redirect(
            method = GET_VELOCITY,
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ryanhcode/sable/api/physics/handle/RigidBodyHandle;getAngularVelocity(Lorg/joml/Vector3d;)Lorg/joml/Vector3d;"))
    private Vector3d nexa$angularVelocityOrZero(final RigidBodyHandle handle, final Vector3d dest) {
        return handle.isValid() ? handle.getAngularVelocity(dest) : dest.zero();
    }
}

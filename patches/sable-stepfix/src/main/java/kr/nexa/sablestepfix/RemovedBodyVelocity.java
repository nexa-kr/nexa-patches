package kr.nexa.sablestepfix;

import com.mojang.logging.LogUtils;
import org.joml.Vector3d;
import org.slf4j.Logger;

/**
 * 이미 제거된 하위 레벨 강체의 속도를 0 으로 본다.
 *
 * <p>Sable 2.0.6 에서 엔티티가 막 제거된 하위 레벨(비행선 등) 위를 지나면 {@code LivingEntity.sable$computeCollisionEffects}
 * → {@code ActiveSableCompanion.getVelocity} → {@code RigidBodyHandle.getLinearVelocity} 로 강체 속도를 묻는다.
 * 강체가 이미 제거됐으면 {@code RapierPhysicsPipeline.assertBodyValid} 가 {@code RuntimeException("Body has been removed")} 을
 * 던져 서버가 "Ticking entity" 로 꺼진다(2026-11-03 06:44 UTC, 엔더맨).
 *
 * <p>{@code assertBodyValid} 의 조건({@code PhysicsPipelineBody.isRemoved()})과 똑같은 {@code RigidBodyHandle.isValid()} 를 먼저
 * 확인하므로 다른 예외는 그대로 올라간다. 사라진 물체는 움직이지 않으니 속도 0 이 맞다.
 */
public final class RemovedBodyVelocity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int LOGGED_QUERIES = 5;
    private static int queries;

    private RemovedBodyVelocity() {}

    /** {@code dest} 를 0 으로 채워 돌려준다(실행당 처음 5번만 기록). */
    public static Vector3d zero(final Vector3d dest) {
        if (queries++ < LOGGED_QUERIES) {
            LOGGER.info("Velocity of a removed sub-level body queried; returning zero instead of throwing");
        }
        return dest.zero();
    }
}

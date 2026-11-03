package kr.nexa.sablestepfix;

import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback.CollisionResult;
import dev.ryanhcode.sable.physics.callback.FragileBlockCallback;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.slf4j.Logger;

/**
 * 약한 블록 파괴를 물리 스텝이 끝난 뒤로 미룬다.
 *
 * <p>Sable 2.0.6 의 네이티브 {@code Rapier3D.step} 은 스텝 동안 물리 상태 읽기 잠금을 쥔 채 접촉 콜백(Java)을 부른다.
 * 콜백이 블록을 부숴 이웃 블록이 이번 실행에서 처음 보는 블록 상태로 바뀌면 {@code Rapier3D.newVoxelCollider} 가 같은 잠금의
 * 쓰기 잠금을 요청해 같은 스레드가 영원히 기다린다(서버 Watchdog 60초 크래시).
 *
 * <p>{@link FragileBlockCallback#onHit} 을 재정의하지 않은 콜백(Sable 기본 약한 블록, treephysics 나뭇잎)만 미룬다.
 * 이 기본 구현은 언제나 "이번 접촉 무시"({@code removeCollision=true})를 돌려주므로 결과를 미리 알 수 있다.
 * {@code onHit} 을 재정의한 콜백은 결과가 다를 수 있어 원래대로 바로 실행한다.
 *
 * <p>미룬 파괴는 같은 서브스텝에서 네이티브 스텝이 끝난 직후, Sable 이 하위 레벨 제거를 처리하기 전에 실행한다
 * ({@code SubLevelPhysicsSystemMixin}). 모든 접근은 서버 스레드에서 일어난다.
 */
public final class DeferredFragileHits {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int LOGGED_DEFERRALS = 5;
    private static final CollisionResult IGNORE_CONTACT = new CollisionResult(new Vector3d(), true);
    private static final Map<Class<?>, Boolean> USES_BASE_ON_HIT = new ConcurrentHashMap<>();
    private static final Map<ServerLevel, Map<BlockPos, Pending>> PENDING = new HashMap<>();
    private static int deferrals;

    private record Pending(FragileBlockCallback callback, BlockState state, Vector3d hitPos) {}

    private DeferredFragileHits() {}

    /** {@code FragileBlockCallback.sable$onCollision} 안의 {@code onHit} 호출을 대신한다. */
    public static CollisionResult onHit(
            final FragileBlockCallback callback,
            final ServerLevel level,
            final BlockPos pos,
            final BlockState state,
            final Vector3d hitPos) {
        if (!SubLevelPhysicsSystem.IN_PHYSICS_STEP || !usesBaseOnHit(callback.getClass())) {
            return callback.onHit(level, pos, state, hitPos);
        }
        final Map<BlockPos, Pending> pending = PENDING.computeIfAbsent(level, key -> new LinkedHashMap<>());
        if (pending.putIfAbsent(pos.immutable(), new Pending(callback, state, new Vector3d(hitPos))) == null
                && deferrals++ < LOGGED_DEFERRALS) {
            LOGGER.info("Deferred fragile block break during physics step: {} at {} ({})",
                    state, pos, callback.getClass().getName());
        }
        return IGNORE_CONTACT;
    }

    /** 네이티브 스텝이 끝나 잠금이 풀린 뒤 미뤄 둔 파괴를 실행한다. */
    public static void flush(final ServerLevel level) {
        final Map<BlockPos, Pending> pending = PENDING.remove(level);
        if (pending == null) {
            return;
        }
        for (final Map.Entry<BlockPos, Pending> entry : pending.entrySet()) {
            final BlockPos pos = entry.getKey();
            final Pending hit = entry.getValue();
            if (level.getBlockState(pos) != hit.state()) {
                continue; // 같은 스텝의 다른 파괴 등으로 이미 바뀜
            }
            try {
                hit.callback().onHit(level, pos, hit.state(), hit.hitPos());
            } catch (final RuntimeException exception) {
                LOGGER.error("Deferred fragile block break failed: {} at {}", hit.state(), pos, exception);
            }
        }
    }

    public static void clear() {
        PENDING.clear();
        deferrals = 0;
    }

    private static boolean usesBaseOnHit(final Class<?> type) {
        return USES_BASE_ON_HIT.computeIfAbsent(type, key -> {
            try {
                return key.getMethod("onHit", ServerLevel.class, BlockPos.class, BlockState.class, Vector3d.class)
                        .getDeclaringClass() == FragileBlockCallback.class;
            } catch (final NoSuchMethodException exception) {
                return false;
            }
        });
    }
}

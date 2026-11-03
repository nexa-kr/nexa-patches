package kr.nexa.sablestepfix.mixin;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import kr.nexa.sablestepfix.DeferredFragileHits;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 네이티브 스텝({@code pipeline.physicsTick})이 돌아와 잠금이 풀린 직후, Sable 이 스텝 중 파괴로 생긴 하위 레벨 제거를
 * 처리하기({@code processSubLevelRemovals}) 바로 전에 미뤄 둔 파괴를 실행한다. 원래 흐름과 같은 자리에서 제거가 처리된다.
 */
@Mixin(value = SubLevelPhysicsSystem.class, remap = false)
public abstract class SubLevelPhysicsSystemMixin {
    @Inject(
            method = "tickPipelinePhysics",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ryanhcode/sable/api/sublevel/ServerSubLevelContainer;processSubLevelRemovals()V"))
    private void nexa$flushDeferredFragileHits(final ServerSubLevelContainer container, final CallbackInfo ci) {
        DeferredFragileHits.flush(((SubLevelPhysicsSystem) (Object) this).getLevel());
    }
}

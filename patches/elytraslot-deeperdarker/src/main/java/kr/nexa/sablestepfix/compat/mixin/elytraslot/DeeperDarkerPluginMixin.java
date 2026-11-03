package kr.nexa.sablestepfix.compat.mixin.elytraslot;

import com.kyanite.deeperdarker.DeeperDarkerConfig;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * {@link SoulElytraBoostPayloadMixin} 과 같은 깨진 필드 읽기가 {@code DeeperDarkerPlugin.tick} 의 쿨다운 안내 문구에도 있다.
 * 이 경로는 클라이언트({@code level.isClientSide()})에서만 돌아 서버에서는 실행되지 않지만, 같은 결함이라 함께 바꾼다.
 */
@Mixin(targets = "com.illusivesoulworks.elytraslot.common.integration.deeperdarker.DeeperDarkerPlugin", remap = false)
public abstract class DeeperDarkerPluginMixin {
    @Redirect(
            method = "tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETSTATIC,
                    target = "Lcom/kyanite/deeperdarker/DeeperDarkerConfig;soulElytraCooldown:I"),
            require = 1,
            allow = 1)
    private static int nexa$soulElytraCooldown() {
        return DeeperDarkerConfig.CONFIG.soulElytraCooldown.getAsInt();
    }
}

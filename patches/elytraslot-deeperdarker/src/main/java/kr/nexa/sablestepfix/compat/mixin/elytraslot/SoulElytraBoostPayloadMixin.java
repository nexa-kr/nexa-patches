package kr.nexa.sablestepfix.compat.mixin.elytraslot;

import com.kyanite.deeperdarker.DeeperDarkerConfig;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Elytra Slot 9.0.2 의 Deeper Darker 연동(소울 겉날개를 겉날개 칸에 낀 채 부스트)이 Deeper Darker 1.4.1 에서 깨진 것을 고친다.
 *
 * <p>Elytra Slot 은 옛 Deeper Darker 의 {@code static int DeeperDarkerConfig.soulElytraCooldown} 을 읽도록 컴파일됐다.
 * 1.4.1 은 이 값을 {@code DeeperDarkerConfig.CONFIG.soulElytraCooldown}({@code ModConfigSpec.IntValue}, 기본 600틱)으로 옮겨서
 * 부스트 패킷마다 {@code NoSuchFieldError} 가 난다(서버 로그 하루 1,532건, 부스트 안 됨).
 * 패킷 처리 람다 {@code SoulElytraBoostPayload.lambda$handle$0} 안의 두 필드 읽기(-1 = 쿨다운 끔 확인, 쿨다운 적용)를
 * 1.4.1 값으로 바꾼다. Deeper Darker 자신의 {@code SoulElytraBoostPacket} 과 같은 값을 쓴다.
 */
@Mixin(targets = "com.illusivesoulworks.elytraslot.common.integration.deeperdarker.SoulElytraBoostPayload", remap = false)
public abstract class SoulElytraBoostPayloadMixin {
    @Redirect(
            method = "lambda$handle$0(Lnet/neoforged/neoforge/network/handling/IPayloadContext;)V",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETSTATIC,
                    target = "Lcom/kyanite/deeperdarker/DeeperDarkerConfig;soulElytraCooldown:I"),
            require = 2,
            allow = 2)
    private static int nexa$soulElytraCooldown() {
        return DeeperDarkerConfig.CONFIG.soulElytraCooldown.getAsInt();
    }
}

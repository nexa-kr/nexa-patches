package kr.nexa.sablestepfix;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Skybound 서버 전용 수정 모음.
 * <ul>
 *   <li>1.0.0 Sable 2.0.6 의 물리 스텝 자기 교착 회피({@link DeferredFragileHits}).
 *   <li>1.0.1 제거된 강체 속도 조회 크래시 회피({@link RemovedBodyVelocity}).
 *   <li>1.0.2 Elytra Slot × Deeper Darker 호환은 뺐다(patches/elytraslot-deeperdarker, 보류).
 * </ul>
 */
@Mod(SableStepFix.MODID)
public final class SableStepFix {
    public static final String MODID = "nexa_sable_stepfix";

    public SableStepFix() {
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> DeferredFragileHits.clear());
    }
}

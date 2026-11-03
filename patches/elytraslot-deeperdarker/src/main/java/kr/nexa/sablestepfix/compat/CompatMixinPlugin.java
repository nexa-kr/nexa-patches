package kr.nexa.sablestepfix.compat;

import java.util.List;
import java.util.Set;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * 다른 모드끼리의 호환 수정 Mixin({@code nexa_sable_stepfix.compat.mixins.json})은 대상 모드가 모두 있을 때만 건다.
 * 없으면 Mixin 클래스를 아예 읽지 않으므로 Elytra Slot·Deeper Darker 가 없는 서버에서도 그대로 뜬다.
 */
public final class CompatMixinPlugin implements IMixinConfigPlugin {
    private static final String PACKAGE = "kr.nexa.sablestepfix.compat.mixin.";

    @Override
    public void onLoad(final String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        if (mixinClassName.startsWith(PACKAGE + "elytraslot.")) {
            return isLoaded("elytraslot") && isLoaded("deeperdarker");
        }
        return false;
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(
            final String targetClassName,
            final ClassNode targetClass,
            final String mixinClassName,
            final IMixinInfo mixinInfo) {}

    @Override
    public void postApply(
            final String targetClassName,
            final ClassNode targetClass,
            final String mixinClassName,
            final IMixinInfo mixinInfo) {}

    private static boolean isLoaded(final String modId) {
        return LoadingModList.get().getModFileById(modId) != null;
    }
}

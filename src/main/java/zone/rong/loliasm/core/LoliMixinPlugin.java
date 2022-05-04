package zone.rong.loliasm.core;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.loliasm.core.classfactories.BakedQuadRedirectorFactory;

import java.util.List;
import java.util.Set;

public class LoliMixinPlugin implements IMixinConfigPlugin {

    static {
        if (LoliLoadingPlugin.isClient && LoliTransformer.squashBakedQuads) {
            BakedQuadRedirectorFactory.generateRedirectorClass();
        }
    }

    @Override
    public void onLoad(String mixinPackage) { }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!LoliConfig.instance.fixTileEntityOnLoadCME && mixinClassName.equals("zone.rong.loliasm.common.forgefixes.mixins.ChunkMixin")) {
            return false;
        }
        if (!LoliConfig.instance.fasterEntitySpawnPreparation && mixinClassName.equals("zone.rong.loliasm.common.forgefixes.mixins.EntityEntryMixin")) {
            return false;
        }
        if (!LoliConfig.instance.copyScreenshotToClipboard && mixinClassName.equals("zone.rong.loliasm.client.screenshot.mixins.MinecraftMixin")) {
            return false;
        }
        if (!LoliConfig.instance.releaseScreenshotCache && mixinClassName.equals("zone.rong.loliasm.client.screenshot.mixins.ScreenShotHelperMixin")) {
            return false;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}

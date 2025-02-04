package zone.rong.garyasm.core;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import zone.rong.garyasm.config.GaryConfig;
import zone.rong.garyasm.core.classfactories.BakedQuadRedirectorFactory;

import java.util.List;
import java.util.Set;

public class GaryMixinPlugin implements IMixinConfigPlugin {

    static {
        if (GaryLoadingPlugin.isClient && GaryTransformer.squashBakedQuads) {
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
        switch (mixinClassName) {
            case "zone.rong.garyasm.common.forgefixes.mixins.ChunkMixin":
                return GaryConfig.instance.fixTileEntityOnLoadCME;
            case "zone.rong.garyasm.common.forgefixes.mixins.EntityEntryMixin":
                return GaryConfig.instance.fasterEntitySpawnPreparation;
            case "zone.rong.garyasm.common.forgefixes.mixins.DimensionTypeMixin":
                return GaryConfig.instance.fixDimensionTypesInliningCrash;
            case "zone.rong.garyasm.client.screenshot.mixins.MinecraftMixin":
                return GaryConfig.instance.copyScreenshotToClipboard;
            case "zone.rong.garyasm.client.screenshot.mixins.ScreenShotHelperMixin":
                return GaryConfig.instance.releaseScreenshotCache;
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

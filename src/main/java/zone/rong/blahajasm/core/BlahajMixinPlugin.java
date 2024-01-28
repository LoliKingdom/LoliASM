package zone.rong.blahajasm.core;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import zone.rong.blahajasm.config.BlahajConfig;
import zone.rong.blahajasm.core.classfactories.BakedQuadRedirectorFactory;

import java.util.List;
import java.util.Set;

public class BlahajMixinPlugin implements IMixinConfigPlugin {

    static {
        if (BlahajLoadingPlugin.isClient && BlahajTransformer.squashBakedQuads) {
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
            case "zone.rong.blahajasm.common.forgefixes.mixins.ChunkMixin":
                return BlahajConfig.instance.fixTileEntityOnLoadCME;
            case "zone.rong.blahajasm.common.forgefixes.mixins.EntityEntryMixin":
                return BlahajConfig.instance.fasterEntitySpawnPreparation;
            case "zone.rong.blahajasm.common.forgefixes.mixins.DimensionTypeMixin":
                return BlahajConfig.instance.fixDimensionTypesInliningCrash;
            case "zone.rong.blahajasm.client.screenshot.mixins.MinecraftMixin":
                return BlahajConfig.instance.copyScreenshotToClipboard;
            case "zone.rong.blahajasm.client.screenshot.mixins.ScreenShotHelperMixin":
                return BlahajConfig.instance.releaseScreenshotCache;
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

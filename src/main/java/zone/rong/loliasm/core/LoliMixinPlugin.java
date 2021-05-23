package zone.rong.loliasm.core;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.loliasm.core.classfactories.BakedQuadRedirectorFactory;

import java.util.List;
import java.util.Set;

public class LoliMixinPlugin implements IMixinConfigPlugin {

    static {
        BakedQuadRedirectorFactory.generateRedirectorClass();
    }

    boolean isRenderingPackage;

    @Override
    public void onLoad(String mixinPackage) {
        this.isRenderingPackage = mixinPackage.contains("rendering");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (isRenderingPackage) {
            return LoliConfig.instance.optimizeSomeRendering;
        }
        return LoliTransformer.squashBakedQuads;
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

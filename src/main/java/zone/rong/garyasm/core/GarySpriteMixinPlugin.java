package zone.rong.garyasm.core;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import zone.rong.garyasm.GaryLogger;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GarySpriteMixinPlugin implements IMixinConfigPlugin {

    static boolean logged = false;

    @Override
    public void onLoad(String s) { }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String s, String s1) {
        if (!logged) {
            GaryLogger.instance.error("Optifine is installed. On demand sprites won't be activated as Optifine already has Smart Animations.");
            logged = true;
        }
        return !GaryTransformer.isOptifineInstalled;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) { }

    @Override
    public List<String> getMixins() {
        return Collections.emptyList();
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) { }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) { }

}

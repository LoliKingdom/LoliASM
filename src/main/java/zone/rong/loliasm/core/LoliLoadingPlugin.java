package zone.rong.loliasm.core;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.LoliReflector;

import java.lang.management.ManagementFactory;
import java.util.Locale;
import java.util.Map;

@IFMLLoadingPlugin.Name("LoliASM")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class LoliLoadingPlugin implements IFMLLoadingPlugin {

    public static final String VERSION = "2.6";

    public static final boolean isDeobf = FMLLaunchHandler.isDeobfuscatedEnvironment();
    public static final boolean isOptifineInstalled = LoliReflector.doesClassExist("optifine.OptiFineForgeTweaker");
    public static final boolean isVMOpenJ9 = System.getProperty("java.vm.name").toLowerCase(Locale.ROOT).contains("openj9");
    public static final boolean isClient = ((Map) Launch.blackboard.get("launchArgs")).containsKey("--assetIndex");

    public LoliLoadingPlugin() {
        LoliLogger.instance.info("Lolis are on the {}-side.", isClient ? "client" : "server");
        LoliLogger.instance.info("Lolis are loading in some mixins since Rongmario's too lazy to write pure ASM all the time despite the mod being called 'LoliASM'");
        MixinBootstrap.init();
        if (LoliConfig.instance.resourceLocationCanonicalization || LoliConfig.instance.optimizeFMLRemapper) {
            for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                if (arg.equals("-XX:+UseStringDeduplication")) {
                    LoliLogger.instance.error("LoliASM encourages you to remove -XX:+UseStringDeduplication from your JVM arguments as it would have little purpose with LoliASM installed, and may actually degrade performance.");
                }
            }
        }
        Mixins.addConfiguration("mixins.internal.json");
        if (LoliConfig.instance.optimizeRegistries) {
            Mixins.addConfiguration("mixins.registries.json");
        }
        if (LoliConfig.instance.stripNearUselessItemStackFields) {
            Mixins.addConfiguration("mixins.stripitemstack.json");
        }
        if (LoliConfig.instance.lockCodeCanonicalization) {
            Mixins.addConfiguration("mixins.lockcode.json");
        }
        if (LoliConfig.instance.optimizeFurnaceRecipeStore) {
            Mixins.addConfiguration("mixins.recipes.json");
        }
        if (isClient && LoliConfig.instance.optimizeSomeRendering) {
            Mixins.addConfiguration("mixins.rendering.json");
        }
        if (LoliConfig.instance.quickerEnableUniversalBucketCheck) {
            Mixins.addConfiguration("mixins.misc_fluidregistry.json");
        }
        Mixins.addConfiguration("mixins.vanities.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return "zone.rong.loliasm.core.LoliFMLCallHook";
    }

    @Override
    public void injectData(Map<String, Object> data) { }

    @Override
    public String getAccessTransformerClass() {
        return "zone.rong.loliasm.core.LoliTransformer";
    }
}
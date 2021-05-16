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

import java.util.Locale;
import java.util.Map;

@IFMLLoadingPlugin.Name("LoliASM")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class LoliLoadingPlugin implements IFMLLoadingPlugin {

    public static final String VERSION = "2.4.1";

    public static final boolean isDeobf = FMLLaunchHandler.isDeobfuscatedEnvironment();
    public static final boolean isOptifineInstalled = LoliReflector.doesClassExist("optifine.OptiFineForgeTweaker");
    public static final boolean isVMOpenJ9 = System.getProperty("java.vm.name").toLowerCase(Locale.ROOT).contains("openj9");
    public static final boolean isClient = ((Map) Launch.blackboard.get("launchArgs")).containsKey("--assetIndex");

    public LoliLoadingPlugin() {
        LoliLogger.instance.info("Lolis are on the {}-side.", isClient ? "client" : "server");
        LoliLogger.instance.info("Lolis are loading in some mixins since Rongmario's too lazy to write pure ASM all the time despite the mod being called 'LoliASM'");
        MixinBootstrap.init();
        LoliConfig.Data data = LoliConfig.getConfig();
        if (data.optimizeDataStructures) {
            Mixins.addConfiguration("mixins.registries.json");
            Mixins.addConfiguration("mixins.memory.json");
        }
        if (data.optimizeFurnaceRecipes) {
            Mixins.addConfiguration("mixins.recipes.json");
        }
        if (isClient && data.optimizeBitsOfRendering) {
            Mixins.addConfiguration("mixins.rendering.json");
        }
        if (data.miscOptimizations) {
            Mixins.addConfiguration("mixins.misc.json");
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
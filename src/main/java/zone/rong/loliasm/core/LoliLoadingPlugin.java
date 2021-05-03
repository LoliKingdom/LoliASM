package zone.rong.loliasm.core;

import com.google.common.cache.CacheBuilder;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import zone.rong.loliasm.LoliConfig;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.LoliReflector;
import zone.rong.loliasm.api.datastructures.DummyMap;

import java.util.Map;

@IFMLLoadingPlugin.Name("LoliASM")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class LoliLoadingPlugin implements IFMLLoadingPlugin {

    public LoliLoadingPlugin() {
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
        if (data.cleanupLaunchClassLoader) {
            LoliLogger.instance.info("Replacing LaunchClassLoader fields with dummy maps/sets.");
            replaceLaunchClassLoaderFields();
        }
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
        // return null;
    }

    @Override
    public void injectData(Map<String, Object> data) { }

    @Override
    public String getAccessTransformerClass() {
        // return null;
        return "zone.rong.loliasm.core.LoliTransformer";
    }

    void replaceLaunchClassLoaderFields() {
        try {
            LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "cachedClasses").invoke(Launch.classLoader, CacheBuilder.newBuilder().concurrencyLevel(2).weakValues().build().asMap());
            LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "invalidClasses").invokeExact(Launch.classLoader, DummyMap.asSet());
            LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "packageManifests").invoke(Launch.classLoader, DummyMap.of());
            LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "resourceCache").invoke(Launch.classLoader, DummyMap.of());
            LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "negativeResourceCache").invokeExact(Launch.classLoader, DummyMap.asSet());
            LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "EMPTY").invoke(null);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
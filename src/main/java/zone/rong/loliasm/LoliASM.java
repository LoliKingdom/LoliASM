package zone.rong.loliasm;

import com.google.common.cache.CacheBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.HttpUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import zone.rong.loliasm.api.datastructures.DummyMap;
import zone.rong.loliasm.api.datastructures.ResourceCache;
import zone.rong.loliasm.api.mixins.RegistrySimpleExtender;
import zone.rong.loliasm.client.models.MultipartBakedModelCache;
import zone.rong.loliasm.client.models.conditions.CanonicalConditions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static zone.rong.loliasm.LoliASM.VERSION;

@Mod(modid = "loliasm", name = "LoliASM", version = VERSION)
@Mod.EventBusSubscriber
public class LoliASM {

    public static final String VERSION = "2.1";

    public static List<RegistrySimpleExtender> simpleRegistryInstances = new ArrayList<>();

    public LoliASM() {
        if (LoliConfig.getConfig().cleanupLaunchClassLoader) {
            try {
                LoliLogger.instance.info("Cleaning up LaunchClassLoader");
                LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "cachedClasses").invoke(Launch.classLoader, CacheBuilder.newBuilder().concurrencyLevel(2).weakValues().build().asMap());
                LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "invalidClasses").invokeExact(Launch.classLoader, DummyMap.asSet());
                LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "packageManifests").invoke(Launch.classLoader, DummyMap.of());
                LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "resourceCache").invoke(Launch.classLoader, new ResourceCache());
                LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "negativeResourceCache").invokeExact(Launch.classLoader, DummyMap.asSet());
                LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "EMPTY").invoke(null);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    /*
    private static Deduplicator deduplicator;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onModelBake(ModelBakeEvent event) {
        if (deduplicator == null) {
            deduplicator = new Deduplicator();
        }
        IRegistry<ModelResourceLocation, IBakedModel> bakedModels = event.getModelRegistry();
        Set<ModelResourceLocation> keys = bakedModels.getKeys();
        ProgressManager.ProgressBar bar = ProgressManager.push("LoliASM: Optimizing IBakedModels", keys.size());
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (ModelResourceLocation mrl : keys) {
            bar.step(mrl.toString());
            deduplicator.deduplicate(bakedModels.getObject(mrl));
        }
        LoliLogger.instance.info("It took {} to optimize IBakedModels", stopwatch.stop());
    }
     */

    @Mod.EventHandler
    @SuppressWarnings("deprecation")
    public void preInit(FMLPreInitializationEvent event) {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(manager -> {
            CanonicalConditions.destroyCache();
            // MultipartBakedModelCache.destroyCache();
        });
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) throws IOException {
        // Map<ResourceLocation, IModel> cache = (Map<ResourceLocation, IModel>) LoliReflector.resolveFieldGetter(ModelLoaderRegistry.class, "cache").invokeExact();
        // ProgressManager.ProgressBar bar = ProgressManager.push("Optimizing Models", cache.size(), true);
        // deduplicator = null; // Free the deduplicator
        LoliLogger.instance.info("Trimming simple registries");
        HttpUtil.DOWNLOADER_EXECUTOR.execute(() -> {
            simpleRegistryInstances.forEach(RegistrySimpleExtender::trim);
            simpleRegistryInstances = null;
        });
    }

}

package zone.rong.loliasm;

import codechicken.asm.ClassHierarchyManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.HttpUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import zone.rong.loliasm.api.datastructures.DummyMap;
import zone.rong.loliasm.api.datastructures.ResourceCache;
import zone.rong.loliasm.api.mixins.RegistrySimpleExtender;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.loliasm.core.LoliLoadingPlugin;
import zone.rong.loliasm.proxy.CommonProxy;

import java.util.*;

@Mod(modid = "loliasm", name = "LoliASM", version = LoliLoadingPlugin.VERSION)
@Mod.EventBusSubscriber
public class LoliASM {

    @SidedProxy(modId = "loliasm", clientSide = "zone.rong.loliasm.proxy.ClientProxy", serverSide = "zone.rong.loliasm.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static List<RegistrySimpleExtender> simpleRegistryInstances = new ArrayList<>();

    public LoliASM() {
        if (LoliConfig.instance.cleanupLaunchClassLoaderEarly) {
            cleanupLaunchClassLoader();
        }
    }

    @Mod.EventHandler
    public void onConstruct(FMLConstructionEvent event) {
        if (LoliConfig.instance.cleanupChickenASMClassHierarchyManager && LoliReflector.doesClassExist("codechicken.asm.ClassHierarchyManager")) {
            // EXPERIMENTAL: As far as I know, this functionality of ChickenASM isn't actually used by any coremods that depends on ChickenASM
            ClassHierarchyManager.superclasses = new HashMap() {
                @Override
                public Object put(Object key, Object value) {
                    return value;
                }
            };
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
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        LoliLogger.instance.info("Trimming simple registries");
        HttpUtil.DOWNLOADER_EXECUTOR.execute(() -> {
            simpleRegistryInstances.forEach(RegistrySimpleExtender::trim);
            simpleRegistryInstances = null;
        });
        if (LoliConfig.instance.cleanupLaunchClassLoaderEarly) {
            invalidateLaunchClassLoaderCaches();
        } else if (LoliConfig.instance.cleanupLaunchClassLoaderLate) {
            cleanupLaunchClassLoader();
        }
    }

    private void cleanupLaunchClassLoader() {
        try {
            LoliLogger.instance.info("Cleaning up LaunchClassLoader");
            if (LoliConfig.instance.noClassCache) {
                LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "cachedClasses").invoke(Launch.classLoader, DummyMap.of());
            } else if (LoliConfig.instance.weakClassCache) {
                Map<String, Class<?>> oldClassCache = (Map<String, Class<?>>) LoliReflector.resolveFieldGetter(LaunchClassLoader.class, "cachedClasses").invoke(Launch.classLoader);
                Cache<String, Class<?>> newClassCache = CacheBuilder.newBuilder().concurrencyLevel(2).weakValues().build();
                newClassCache.putAll(oldClassCache);
                LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "cachedClasses").invoke(Launch.classLoader, newClassCache.asMap());
            }
            if (LoliConfig.instance.noResourceCache) {
                LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "resourceCache").invoke(Launch.classLoader, new ResourceCache());
                LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "negativeResourceCache").invokeExact(Launch.classLoader, DummyMap.asSet());
            } else if (LoliConfig.instance.weakResourceCache) {
                Map<String, byte[]> oldResourceCache = (Map<String, byte[]>) LoliReflector.resolveFieldGetter(LaunchClassLoader.class, "resourceCache").invoke(Launch.classLoader);
                Cache<String, byte[]> newResourceCache = CacheBuilder.newBuilder().concurrencyLevel(2).weakValues().build();
                newResourceCache.putAll(oldResourceCache);
                LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "resourceCache").invoke(Launch.classLoader, newResourceCache.asMap());
            }
            if (LoliConfig.instance.disablePackageManifestMap) {
                LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "packageManifests").invokeExact(Launch.classLoader, DummyMap.of());
                LoliReflector.resolveFieldSetter(LaunchClassLoader.class, "EMPTY").invoke(null);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void invalidateLaunchClassLoaderCaches() {
        try {
            LoliLogger.instance.info("Invalidating and Cleaning LaunchClassLoader caches");
            if (!LoliConfig.instance.noClassCache) {
                ((Map<String, Class<?>>) LoliReflector.resolveFieldGetter(LaunchClassLoader.class, "cachedClasses").invoke(Launch.classLoader)).clear();
            }
            if (!LoliConfig.instance.noResourceCache) {
                ((Map<String, byte[]>) LoliReflector.resolveFieldGetter(LaunchClassLoader.class, "resourceCache").invoke(Launch.classLoader)).clear();
                ((Set<String>) LoliReflector.resolveFieldGetter(LaunchClassLoader.class, "negativeResourceCache").invoke(Launch.classLoader)).clear();
            }
            ((Set<String>) LoliReflector.resolveFieldGetter(LaunchClassLoader.class, "invalidClasses").invoke(Launch.classLoader)).clear();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

}

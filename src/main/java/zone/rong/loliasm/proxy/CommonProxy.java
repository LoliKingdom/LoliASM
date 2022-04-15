package zone.rong.loliasm.proxy;

import betterwithmods.module.gameplay.Gameplay;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.HttpUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.*;
import zone.rong.loliasm.LoliASM;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.LoliReflector;
import zone.rong.loliasm.api.LoliStringPool;
import zone.rong.loliasm.api.datastructures.DummyMap;
import zone.rong.loliasm.api.datastructures.ResourceCache;
import zone.rong.loliasm.api.mixins.RegistrySimpleExtender;
import zone.rong.loliasm.common.java.JavaFixes;
import zone.rong.loliasm.common.modfixes.betterwithmods.BWMBlastingOilOptimization;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.loliasm.spark.LoliSparker;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

public class CommonProxy {

    public void construct(FMLConstructionEvent event) {
        if (LoliConfig.instance.cleanupLaunchClassLoaderEarly) {
            cleanupLaunchClassLoader();
        }
        /*
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                File classesCache = new File(loliCachesFolder, "classes.bin");
                if (classesCache.createNewFile()) {
                    Map<String, byte[]> classesToCache = new Object2ObjectOpenHashMap<>();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
         */
    }

    public void preInit(FMLPreInitializationEvent event) { }

    public void init(FMLInitializationEvent event) { }

    public void postInit(FMLPostInitializationEvent event) {
        if (LoliConfig.instance.skipCraftTweakerRecalculatingSearchTrees) {
            LoliReflector.getClass("crafttweaker.mc1120.CraftTweaker").ifPresent(c -> {
                try {
                    Field alreadyChangedThePlayer = c.getDeclaredField("alreadyChangedThePlayer");
                    alreadyChangedThePlayer.setAccessible(true);
                    alreadyChangedThePlayer.setBoolean(null, true);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
            });
        }
        if (Loader.isModLoaded("betterwithmods") && LoliConfig.instance.bwmBlastingOilOptimization) {
            if (!Gameplay.disableBlastingOilEvents) {
                MinecraftForge.EVENT_BUS.register(BWMBlastingOilOptimization.class);
            }
        }
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
        LoliLogger.instance.info("Trimming simple registries");
        HttpUtil.DOWNLOADER_EXECUTOR.execute(() -> {
            LoliASM.simpleRegistryInstances.forEach(RegistrySimpleExtender::trim);
            LoliASM.simpleRegistryInstances = null;
        });
        if (LoliConfig.instance.cleanupLaunchClassLoaderEarly || LoliConfig.instance.cleanCachesOnGameLoad) {
            invalidateLaunchClassLoaderCaches();
        } else if (LoliConfig.instance.cleanupLaunchClassLoaderLate) {
            cleanupLaunchClassLoader();
        }
        if (LoliStringPool.getSize() > 0) {
            MinecraftForge.EVENT_BUS.register(LoliStringPool.class);
            LoliLogger.instance.info("{} total strings processed. {} unique strings in LoliStringPool, {} strings deduplicated altogether during game load.", LoliStringPool.getDeduplicatedCount(), LoliStringPool.getSize(), LoliStringPool.getDeduplicatedCount() - LoliStringPool.getSize());
        }
        if (LoliConfig.instance.filePermissionsCacheCanonicalization) {
            MinecraftForge.EVENT_BUS.register(JavaFixes.INSTANCE);
        }
        if (LoliConfig.instance.sparkSummarizeHeapSpaceAfterGameLoads && Loader.isModLoaded("spark")) {
            LoliSparker.checkHeap(true, true);
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

    private static void cleanupLaunchClassLoader() {
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
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}

package zone.rong.garyasm.proxy;

import betterwithmods.module.gameplay.Gameplay;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.*;
import zone.rong.garyasm.GaryASM;
import zone.rong.garyasm.GaryLogger;
import zone.rong.garyasm.GaryReflector;
import zone.rong.garyasm.api.GaryStringPool;
import zone.rong.garyasm.api.datastructures.DummyMap;
import zone.rong.garyasm.api.datastructures.ResourceCache;
import zone.rong.garyasm.api.mixins.RegistrySimpleExtender;
import zone.rong.garyasm.client.GaryIncompatibilityHandler;
import zone.rong.garyasm.common.java.JavaFixes;
import zone.rong.garyasm.common.modfixes.betterwithmods.BWMBlastingOilOptimization;
import zone.rong.garyasm.common.modfixes.ebwizardry.ArcaneLocks;
import zone.rong.garyasm.config.GaryConfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommonProxy {

    /**
     * This has to be called after FMLPreInitializationEvent as FMLConstructionEvent is wrapped in a different try - catch that behaves differently...
     */
    public void throwIncompatibility() {
        boolean texFix = Loader.isModLoaded("texfix");
        boolean vanillaFix = Loader.isModLoaded("vanillafix");
        if (texFix || vanillaFix) {
            List<String> messages = new ArrayList<>();
            messages.add("GaryASM has replaced and improved upon functionalities from the following mods.");
            messages.add("Therefore, these mods are now incompatible with GaryASM:");
            messages.add("");
            if (texFix) {
                messages.add(TextFormatting.BOLD + "TexFix");
            }
            if (vanillaFix) {
                messages.add(TextFormatting.BOLD + "VanillaFix");
            }
            GaryIncompatibilityHandler.garyHaetPizza(messages);
        }
    }

    public void construct(FMLConstructionEvent event) {
        if (GaryConfig.instance.cleanupLaunchClassLoaderEarly) {
            cleanupLaunchClassLoader();
        }
        if (GaryConfig.instance.threadPriorityFix)
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 2);
    }

    public void preInit(FMLPreInitializationEvent event) { }

    public void init(FMLInitializationEvent event) { }

    public void postInit(FMLPostInitializationEvent event) {
        if (GaryConfig.instance.skipCraftTweakerRecalculatingSearchTrees) {
            GaryReflector.getClass("crafttweaker.mc1120.CraftTweaker").ifPresent(c -> {
                try {
                    Field alreadyChangedThePlayer = c.getDeclaredField("alreadyChangedThePlayer");
                    alreadyChangedThePlayer.setAccessible(true);
                    alreadyChangedThePlayer.setBoolean(null, true);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
            });
        }
        if (Loader.isModLoaded("betterwithmods") && GaryConfig.instance.bwmBlastingOilOptimization) {
            if (!Gameplay.disableBlastingOilEvents) {
                MinecraftForge.EVENT_BUS.register(BWMBlastingOilOptimization.class);
            }
        }
        if (Loader.isModLoaded("ebwizardry") && GaryConfig.instance.optimizeArcaneLockRendering) {
            GaryASM.customTileDataConsumer = ArcaneLocks.TRACK_ARCANE_TILES;
        }
    }

    public void loadComplete(FMLLoadCompleteEvent event) {
        GaryLogger.instance.info("Trimming simple registries");
        HttpUtil.DOWNLOADER_EXECUTOR.execute(() -> {
            GaryASM.simpleRegistryInstances.forEach(RegistrySimpleExtender::trim);
            GaryASM.simpleRegistryInstances = null;
        });
        if (GaryConfig.instance.cleanupLaunchClassLoaderEarly || GaryConfig.instance.cleanCachesOnGameLoad) {
            invalidateLaunchClassLoaderCaches();
        } else if (GaryConfig.instance.cleanupLaunchClassLoaderLate) {
            cleanupLaunchClassLoader();
        }
        if (GaryStringPool.getSize() > 0) {
            MinecraftForge.EVENT_BUS.register(GaryStringPool.class);
            GaryLogger.instance.info("{} total strings processed. {} unique strings in GaryStringPool, {} strings deduplicated altogether during game load.", GaryStringPool.getDeduplicatedCount(), GaryStringPool.getSize(), GaryStringPool.getDeduplicatedCount() - GaryStringPool.getSize());
        }
        if (GaryConfig.instance.filePermissionsCacheCanonicalization) {
            MinecraftForge.EVENT_BUS.register(JavaFixes.INSTANCE);
        }
    }

    private void invalidateLaunchClassLoaderCaches() {
        try {
            GaryLogger.instance.info("Invalidating and Cleaning LaunchClassLoader caches");
            if (!GaryConfig.instance.noClassCache) {
                ((Map<String, Class<?>>) GaryReflector.resolveFieldGetter(LaunchClassLoader.class, "cachedClasses").invoke(Launch.classLoader)).clear();
            }
            if (!GaryConfig.instance.noResourceCache) {
                ((Map<String, byte[]>) GaryReflector.resolveFieldGetter(LaunchClassLoader.class, "resourceCache").invoke(Launch.classLoader)).clear();
                ((Set<String>) GaryReflector.resolveFieldGetter(LaunchClassLoader.class, "negativeResourceCache").invoke(Launch.classLoader)).clear();
            }
            ((Set<String>) GaryReflector.resolveFieldGetter(LaunchClassLoader.class, "invalidClasses").invoke(Launch.classLoader)).clear();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private static void cleanupLaunchClassLoader() {
        try {
            GaryLogger.instance.info("Cleaning up LaunchClassLoader");
            if (GaryConfig.instance.noClassCache) {
                GaryReflector.resolveFieldSetter(LaunchClassLoader.class, "cachedClasses").invoke(Launch.classLoader, DummyMap.of());
            } else if (GaryConfig.instance.weakClassCache) {
                Map<String, Class<?>> oldClassCache = (Map<String, Class<?>>) GaryReflector.resolveFieldGetter(LaunchClassLoader.class, "cachedClasses").invoke(Launch.classLoader);
                Cache<String, Class<?>> newClassCache = CacheBuilder.newBuilder().concurrencyLevel(2).weakValues().build();
                newClassCache.putAll(oldClassCache);
                GaryReflector.resolveFieldSetter(LaunchClassLoader.class, "cachedClasses").invoke(Launch.classLoader, newClassCache.asMap());
            }
            if (GaryConfig.instance.noResourceCache) {
                GaryReflector.resolveFieldSetter(LaunchClassLoader.class, "resourceCache").invoke(Launch.classLoader, new ResourceCache());
                GaryReflector.resolveFieldSetter(LaunchClassLoader.class, "negativeResourceCache").invokeExact(Launch.classLoader, DummyMap.asSet());
            } else if (GaryConfig.instance.weakResourceCache) {
                Map<String, byte[]> oldResourceCache = (Map<String, byte[]>) GaryReflector.resolveFieldGetter(LaunchClassLoader.class, "resourceCache").invoke(Launch.classLoader);
                Cache<String, byte[]> newResourceCache = CacheBuilder.newBuilder().concurrencyLevel(2).weakValues().build();
                newResourceCache.putAll(oldResourceCache);
                GaryReflector.resolveFieldSetter(LaunchClassLoader.class, "resourceCache").invoke(Launch.classLoader, newResourceCache.asMap());
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}

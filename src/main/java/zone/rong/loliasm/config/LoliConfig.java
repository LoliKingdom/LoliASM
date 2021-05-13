package zone.rong.loliasm.config;

import com.google.common.base.Strings;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.launchwrapper.Launch;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.LoliReflector;
import zone.rong.loliasm.config.annotation.*;
import zone.rong.loliasm.core.LoliLoadingPlugin;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class LoliConfig {

    private static Data config;

    public static class Data {

        final String VERSION;

        @Ignore final String bakedQuadsSquasherComment = "Saves memory by optimizing BakedQuads with creation of new BakedQuad classes to squash variables down.";
        @Since("1.0") public final boolean bakedQuadsSquasher;

        @Ignore final String logClassesThatNeedPatchingComment = "Turn this on to log any callers using old BakedQuads constructors. Check logs and put them in the 'bakedQuadPatchClasses' list.";
        @Since("1.0") public final boolean logClassesThatNeedPatching;

        @Ignore final String bakedQuadPatchClassesComment = "List any classes using old BakedQuad constructors. 'logClassesThatNeedPatching' is crucial in identifying the classes.";
        @Since("1.0") public final String[] bakedQuadPatchClasses;

        @Ignore final String cleanupLaunchClassLoaderComment = "Experimental: Saves memory from cleaning out redundant caches in Mojang's LaunchClassLoader. Will impact loading time by a bit.";
        @Since("2.0") public final boolean cleanupLaunchClassLoader;

        @Ignore final String remapperMemorySaverComment = "Experimental: Saves memory by canonizing strings cached in the remapper. May impact loading time by a little.";
        @Since("2.0") public final boolean remapperMemorySaver;

        @Ignore final String canonizeObjectsComment = "Experimental: Saves memory by pooling different Object instances and deduplicating them from different locations such as ResourceLocations, IBakedModels.";
        @Since("2.0") public final boolean canonizeObjects;

        @Ignore final String optimizeDataStructuresComment = "Saves memory by optimizing various data structures around Minecraft, MinecraftForge and mods.";
        @Since("2.0") public final boolean optimizeDataStructures;

        @Ignore final String optimizeFurnaceRecipesComment = "Saves memory and furnace recipe search time by optimizing FurnaceRecipes' algorithm.";
        @Since("2.0") public final boolean optimizeFurnaceRecipes;

        @Ignore final String optimizeBitsOfRenderingComment = "Optimizes certain aspects of the Client/Rendering Thread.";
        @Since("2.3") public final boolean optimizeBitsOfRendering;

        @Ignore final String miscOptimizationsComment = "Other optimization tweaks. Nothing that is experimental or has breaking changes would be classed under this.";
        @Since("2.3.1") public final boolean miscOptimizations;

        @Ignore final String modFixesComment = "Various mod fixes and optimizations.";
        @Since("2.4") public final boolean modFixes;

        public Data(String version,
                    boolean bakedQuadsSquasher,
                    boolean logClassesThatNeedPatching,
                    String[] bakedQuadPatchClasses,
                    boolean cleanupLaunchClassLoader,
                    boolean remapperMemorySaver,
                    boolean canonizeObjects,
                    boolean optimizeDataStructures,
                    boolean optimizeFurnaceRecipes,
                    boolean optimizeBitsOfRendering,
                    boolean miscOptimizations,
                    boolean modFixes) {
            this.VERSION = version;
            this.bakedQuadsSquasher = bakedQuadsSquasher;
            this.logClassesThatNeedPatching = logClassesThatNeedPatching;
            this.bakedQuadPatchClasses = bakedQuadPatchClasses;
            this.cleanupLaunchClassLoader = cleanupLaunchClassLoader;
            this.remapperMemorySaver = remapperMemorySaver;
            this.canonizeObjects = canonizeObjects;
            this.optimizeDataStructures = optimizeDataStructures;
            this.optimizeFurnaceRecipes = optimizeFurnaceRecipes;
            this.optimizeBitsOfRendering = optimizeBitsOfRendering;
            this.miscOptimizations = miscOptimizations;
            this.modFixes = modFixes;
        }
    }

    public static Data getConfig() {
        if (config != null) {
            return config;
        }
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .addDeserializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getAnnotation(Ignore.class) != null;
                    }
                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();
        File configFile = new File(Launch.minecraftHome, "config" + File.separator + "loliasm.json");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (FileWriter writer = new FileWriter(configFile)) {
                    config = new Data(LoliLoadingPlugin.VERSION, true, true, new String[] { "net.minecraft.client.renderer.block.model.FaceBakery" }, true, true, true, true, true, true, true, true);
                    gson.toJson(config, writer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (FileReader reader = new FileReader(configFile)) {
                config = gson.fromJson(reader, Data.class);
                if (Strings.isNullOrEmpty(config.VERSION)) {
                    LoliLogger.instance.info("Config missing VERSION, writing current LoliASM version {} to config.", LoliLoadingPlugin.VERSION);
                    config = new Data(LoliLoadingPlugin.VERSION,
                            config.bakedQuadsSquasher,
                            config.logClassesThatNeedPatching,
                            config.bakedQuadPatchClasses,
                            config.cleanupLaunchClassLoader,
                            config.remapperMemorySaver,
                            config.canonizeObjects,
                            config.optimizeDataStructures,
                            config.optimizeFurnaceRecipes,
                            true,
                            true,
                            true);
                    try (FileWriter writer = new FileWriter(configFile)) {
                        gson.toJson(config, writer);
                    }
                } else if (isVersionOutdated(config.VERSION, LoliLoadingPlugin.VERSION)) {
                    LoliLogger.instance.info("Config outdated, updating config from version {} to {}.", config.VERSION, LoliLoadingPlugin.VERSION);
                    MethodHandle dataCtor = LoliReflector.resolveCtor(Data.class, String.class, boolean.class, boolean.class, String[].class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class, boolean.class);
                    List<Object> args = new ArrayList<>();
                    args.add(LoliLoadingPlugin.VERSION);
                    for (Field field : Data.class.getFields()) {
                        String sinceVersion = field.getAnnotation(Since.class).value();
                        if (field.getName().equals("bakedQuadPatchClasses")) { // Special case is ugly
                            args.add(isVersionOutdated(config.VERSION, sinceVersion) ? new String[] { "net.minecraft.client.renderer.block.model.FaceBakery" } : field.get(config));
                        } else {
                            args.add(isVersionOutdated(config.VERSION, sinceVersion) ? true : field.get(config)); // Default to true if version is outdated
                        }
                    }
                    config = (Data) dataCtor.invokeWithArguments(args);
                    try (FileWriter writer = new FileWriter(configFile)) {
                        gson.toJson(config, writer);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return config;
    }

    private static boolean isVersionOutdated(String configVersion, String targetVersion) {
        return new DefaultArtifactVersion(configVersion).compareTo(new DefaultArtifactVersion(targetVersion)) < 0;
    }
}

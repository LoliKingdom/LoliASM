package zone.rong.loliasm;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Config;

import java.io.*;

public class LoliConfig {

    private static Data config;

    public static class Data {

        @Config.Ignore
        final String bakedQuadsSquasherComment = "Saves memory by optimizing BakedQuads with creation of new BakedQuad classes to squash variables down.";
        final boolean bakedQuadsSquasher;

        @Config.Ignore
        final String logClassesThatNeedPatchingComment = "Turn this on to log any callers using old BakedQuads constructors. Check logs and put them in the 'bakedQuadPatchClasses' list.";
        public final boolean logClassesThatNeedPatching;

        @Config.Ignore
        final String bakedQuadPatchClassesComment = "List any classes using old BakedQuad constructors. 'logClassesThatNeedPatching' is crucial in identifying the classes.";
        public final String[] bakedQuadPatchClasses;

        @Config.Ignore
        final String cleanupLaunchClassLoaderComment = "Experimental: Saves memory from cleaning out redundant maps and collections in Mojang's LaunchClassLoader.";
        public final boolean cleanupLaunchClassLoader;

        @Config.Ignore
        final String remapperMemorySaverComment = "Experimental: Saves memory by canonizing strings cached in the remapper. May impact loading time by a small amount.";
        public final boolean remapperMemorySaver;

        @Config.Ignore
        final String canonizeObjectsComment = "Experimental: Saves memory by pooling different Object instances and deduplicating them from different locations such as ResourceLocations, IBakedModels.";
        public final boolean canonizeObjects;

        @Config.Ignore
        final String optimizeDataStructuresComment = "Saves memory by optimizing various data structures around Minecraft, MinecraftForge and mods.";
        public final boolean optimizeDataStructures;

        @Config.Ignore
        final String optimizeFurnaceRecipesComment = "Saves memory and furnace recipe search time by optimizing FurnaceRecipes' algorithm.";
        public final boolean optimizeFurnaceRecipes;

        public Data(boolean bakedQuadsSquasher,
                    boolean logClassesThatNeedPatching,
                    String[] bakedQuadPatchClasses,
                    boolean cleanupLaunchClassLoader,
                    boolean remapperMemorySaver,
                    boolean canonizeObjects,
                    boolean optimizeDataStructures,
                    boolean optimizeFurnaceRecipes) {
            this.bakedQuadsSquasher = bakedQuadsSquasher;
            this.logClassesThatNeedPatching = logClassesThatNeedPatching;
            this.bakedQuadPatchClasses = bakedQuadPatchClasses;
            this.cleanupLaunchClassLoader = cleanupLaunchClassLoader;
            this.remapperMemorySaver = remapperMemorySaver;
            this.canonizeObjects = canonizeObjects;
            this.optimizeDataStructures = optimizeDataStructures;
            this.optimizeFurnaceRecipes = optimizeFurnaceRecipes;
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
                        return f.getAnnotation(Config.Ignore.class) != null;
                    }
                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();
        Data data = null;
        File configFile = new File(Launch.minecraftHome, "config/loliasm.json");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (FileWriter writer = new FileWriter(configFile)) {
                    data = config = new Data(true, true, new String[] { "net.minecraft.client.renderer.block.model.FaceBakery" }, true, true, true, true, true);
                    gson.toJson(data, writer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (FileReader reader = new FileReader(configFile)) {
                data = config = gson.fromJson(reader, Data.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

}

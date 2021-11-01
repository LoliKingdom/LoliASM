package zone.rong.loliasm.config;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.config.annotation.*;

import java.io.*;
import java.util.Arrays;
import java.util.Set;

public class LoliConfig {

    public static final LoliConfig instance = new LoliConfig();

    static {
        instance.initialize();
        File oldConfigFile = new File(Launch.minecraftHome, "config" + File.separator + "loliasm.json");
        if (oldConfigFile.exists()) {
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
            try (FileReader reader = new FileReader(oldConfigFile)) {
                Data oldConfig = gson.fromJson(reader, Data.class);
                instance.squashBakedQuads = instance.setBoolean("squashBakedQuads", "bakedquad", oldConfig.bakedQuadsSquasher);
                instance.classesThatCallBakedQuadCtor = instance.setStringArray("classesThatCallBakedQuadCtor", "bakedquad", oldConfig.bakedQuadPatchClasses);
                instance.logClassesThatCallBakedQuadCtor = instance.setBoolean("logClassesThatCallBakedQuadCtor", "bakedquad", oldConfig.logClassesThatNeedPatching);
                instance.cleanupLaunchClassLoaderLate = instance.setBoolean("cleanupLaunchClassLoaderLate", "launchwrapper", oldConfig.cleanupLaunchClassLoader);
                instance.cleanupLaunchClassLoaderLate = instance.setBoolean("cleanupLaunchClassLoaderLate", "launchwrapper", oldConfig.cleanupLaunchClassLoader);
                instance.optimizeFMLRemapper = instance.setBoolean("optimizeFMLRemapper", "remapper", oldConfig.remapperMemorySaver);
                instance.optimizeRegistries = instance.setBoolean("optimizeRegistries", "datastructures", oldConfig.optimizeDataStructures);
                instance.optimizeFurnaceRecipeStore = instance.setBoolean("optimizeFurnaceRecipeStore", "datastructures", oldConfig.optimizeFurnaceRecipes);
                instance.optimizeSomeRendering = instance.setBoolean("optimizeSomeRendering", "rendering", oldConfig.optimizeBitsOfRendering);
                instance.quickerEnableUniversalBucketCheck = instance.setBoolean("quickerEnableUniversalBucketCheck", "misc", oldConfig.miscOptimizations);
                instance.stripInstancedRandomFromSoundEventAccessor = instance.setBoolean("stripInstancedRandomFromSoundEventAccessor", "misc", oldConfig.miscOptimizations);
                instance.fixBlockIEBaseArrayIndexOutOfBoundsException = instance.setBoolean("fixBlockIEBaseArrayIndexOutOfBoundsException", "modfixes", oldConfig.modFixes);
                instance.configuration.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
            oldConfigFile.delete();
        }
    }

    private Configuration configuration;

    public boolean squashBakedQuads, logClassesThatCallBakedQuadCtor, reuseBucketQuads;
    public String[] classesThatCallBakedQuadCtor, classesThatExtendBakedQuad;
    public boolean cleanupLaunchClassLoaderEarly, cleanupLaunchClassLoaderLate, noResourceCache, noClassCache, weakResourceCache, weakClassCache, disablePackageManifestMap, cleanCachesOnGameLoad/*, cleanCachesOnWorldLoad*/;
    public boolean resourceLocationCanonicalization, modelConditionCanonicalization, nbtTagStringBackingStringCanonicalization, nbtBackingMapStringCanonicalization, packageStringCanonicalization, lockCodeCanonicalization, spriteNameCanonicalization, asmDataStringCanonicalization, vertexDataCanonicalization, filePermissionsCacheCanonicalization;
    public boolean optimizeFMLRemapper;
    public boolean optimizeRegistries, optimizeNBTTagCompoundBackingMap, optimizeFurnaceRecipeStore, stripNearUselessItemStackFields, moreModelManagerCleanup, optimizeAndCacheJEISearchTrees;
    public boolean releaseSpriteFramesCache;
    public boolean optimizeSomeRendering, stripUnnecessaryLocalsInRenderHelper;
    public boolean quickerEnableUniversalBucketCheck, stripInstancedRandomFromSoundEventAccessor, classCaching, copyScreenshotToClipboard;
    public boolean fixBlockIEBaseArrayIndexOutOfBoundsException, cleanupChickenASMClassHierarchyManager, optimizeAmuletRelatedFunctions, labelCanonicalization, skipCraftTweakerRecalculatingSearchTrees, bwmBlastingOilOptimization, optimizeQMDBeamRenderer, repairEvilCraftEIOCompat;
    public boolean fixAmuletHolderCapability;
    public boolean fixFillBucketEventNullPointerException, fixTileEntityOnLoadCME, removeForgeSecurityManager, fasterEntitySpawnPreparation;
    public boolean sparkProfileEntireGameLoad, sparkProfileCoreModLoading, sparkProfileConstructionStage, sparkProfilePreInitializationStage, sparkProfileInitializationStage, sparkProfilePostInitializationStage, sparkProfileLoadCompleteStage, includeAllThreadsWhenProfiling;

    private void initialize() {
        configuration = new Configuration(new File(Launch.minecraftHome, "config" + File.separator + "loliasm.cfg"));
        load();
    }

    public void load() {
        squashBakedQuads = getBoolean("squashBakedQuads", "bakedquad", "Saves RAM by removing BakedQuad instance variables, redirecting BakedQuad creation to specific BakedQuad child classes. This will be forcefully turned off when Optifine is installed as it is incompatible", true);
        classesThatCallBakedQuadCtor = getStringArray("classesThatCallBakedQuadCtor", "bakedquad", "Classes where BakedQuad::new calls need to be redirected. As of 3.2, this should be done automatically, while the changes will show in the next launch", "net.minecraft.client.renderer.block.model.FaceBakery");
        classesThatExtendBakedQuad = getStringArray("classesThatExtendBakedQuad", "bakedquad", "Classes that extend BakedQuad need to be handled separately. This should be done automatically, while the changes will show in the next launch", "");
        logClassesThatCallBakedQuadCtor = getBoolean("logClassesThatCallBakedQuadCtor", "bakedquad", "Log classes that need their BakedQuad::new calls redirected", true);
        reuseBucketQuads = getBoolean("reuseBucketQuads", "bakedquad", "Allows bucket models to re-use UnpackedBakedQuads", true);

        cleanupLaunchClassLoaderEarly = getBoolean("cleanupLaunchClassLoaderEarly", "launchwrapper", "Cleanup some redundant data structures in LaunchClassLoader at the earliest point possible (when LoliASM is loaded). Helpful for those that don't have enough RAM to load into the game. This can induce slowdowns while loading the game in exchange for more available RAM", false);
        cleanupLaunchClassLoaderLate = getBoolean("cleanupLaunchClassLoaderLate", "launchwrapper", "Cleanup some redundant data structures in LaunchClassLoader at the latest point possible (when the game reaches the Main Screen). This is for those that have enough RAM to load the game and do not want any slowdowns while loading. Note: if 'cleanupLaunchClassLoaderEarly' is 'true', this option will be ignored", true);
        noResourceCache = getBoolean("noResourceCache", "launchwrapper", "Disabling caching of resources (Class Bytes). This will induce slowdowns to game/world loads in exchange for more available RAM", false);
        noClassCache = getBoolean("noClassCache", "launchwrapper", "Disabling caching of classes. This will induce major slowdowns to game/world loads in exchange for more available RAM", false);
        weakResourceCache = getBoolean("weakResourceCache", "launchwrapper", "Weaken the caching of resources (Class Bytes). This allows the GC to free up more space when the caches are no longer needed. If 'noResourceCache' is 'true', this option will be ignored. This option coincides with Foamfix's 'weakenResourceCache' option", true);
        weakClassCache = getBoolean("weakClassCache", "launchwrapper", "Weaken the caching of classes. This allows the GC to free up more space when the caches are no longer needed. If 'noClassCache' is 'true', this option will be ignored", true);
        disablePackageManifestMap = getBoolean("disablePackageManifestMap", "launchwrapper", "Disable the unusused Package Manifest map. This option coincides with Foamfix's 'removePackageManifestMap' option", true);
        cleanCachesOnGameLoad = getBoolean("cleanCachesOnGameLoad", "launchwrapper", "Invalidate and clean cache entries when the game finishes loading (onto the main screen). Loading into the first world may take longer. This option wouldn't do anything if 'cleanupLaunchClassLoaderLate' is 'true'", false);
        // cleanCachesOnWorldLoad = getBoolean("cleanCachesOnWorldLoad", "launchwrapper", "Invalidate and clean cache entries when you load into a world, whether that be loading into a singleplayer world or a multiplayer server.", true);

        resourceLocationCanonicalization = getBoolean("resourceLocationCanonicalization", "canonicalization", "Deduplicate ResourceLocation and ModelResourceLocation instances", true);
        modelConditionCanonicalization = getBoolean("modelConditionCanonicalization", "canonicalization", "Deduplicate Model Conditions. Enable this if you do not have Foamfix installed", false);
        nbtTagStringBackingStringCanonicalization = getBoolean("nbtTagStringBackingStringCanonicalization", "canonicalization", "Deduplicate Strings in NBTTagString", true);
        nbtBackingMapStringCanonicalization = getBoolean("nbtBackingMapStringCanonicalization", "canonicalization", "Deduplicate String keys in NBTTagCompound", true);
        packageStringCanonicalization = getBoolean("packageStringCanonicalization", "canonicalization", "Deduplicate package strings when Forge gathers them when mod candidates are loaded", true);
        lockCodeCanonicalization = getBoolean("lockCodeCanonicalization", "canonicalization", "Deduplicate LockCode when reading from NBT", true);
        spriteNameCanonicalization = getBoolean("spriteNameCanonicalization", "canonicalization", "Deduplicate TextureAtlasSprite's names", true);
        asmDataStringCanonicalization = getBoolean("asmDataStringCanonicalization", "canonicalization", "Deduplicate ASMData related Strings", true);
        vertexDataCanonicalization = getBoolean("vertexDataCanonicalization", "canonicalization", "EXPERIMENTAL: Deduplicate BakedQuad's Vertex Data array. If you see any artifacting in-game, turn this off and report it on github", true);
        filePermissionsCacheCanonicalization = getBoolean("filePermissionsCacheCanonicalization", "canonicalization", "Deduplicate Java's FilePermission cache's names within SecureClassLoader", true);

        optimizeFMLRemapper = getBoolean("optimizeFMLRemapper", "remapper", "Optimizing Forge's Remapper for not storing redundant entries", true);

        // optimizeDataStructures = getBoolean("optimizeDataStructures", "datastructures", "Optimizes various data structures around Minecraft", true);
        optimizeRegistries = getBoolean("optimizeRegistries", "datastructures", "Optimizes registries", true);
        optimizeNBTTagCompoundBackingMap = getBoolean("optimizeNBTTagCompoundBackingMap", "datastructures", "Optimize NBTTagCompound's backing map structure", true);
        optimizeFurnaceRecipeStore = getBoolean("optimizeFurnaceRecipeStore", "datastructures", "Optimizing FurnaceRecipes. FastFurnace will see very little benefit when this option is turned on", true);
        stripNearUselessItemStackFields = getBoolean("stripNearUselessItemStackFields", "datastructures", "EXPERIMENTAL: Strips ItemStack of some of its fields as it stores some near-useless references", true);
        moreModelManagerCleanup = getBoolean("moreModelManagerCleanup", "datastructures", "Clears and trims ModelManager data structures after models are loaded and baked", true);
        optimizeAndCacheJEISearchTrees = getBoolean("optimizeAndCacheJEISearchTrees", "datastructures", "When JEI is installed, optimize search tree structures along with caching to disk for insane loadtime optimization", true);

        releaseSpriteFramesCache = getBoolean("releaseSpriteFramesCache", "textures", "Releases TextureAtlasSprite's framesTextureData. Won't touch custom TextureAtlasSprite implementations", true);

        optimizeSomeRendering = getBoolean("optimizeSomeRendering", "rendering", "Optimizes some rendering features, not game-breaking; however, negligible at times", true);
        stripUnnecessaryLocalsInRenderHelper = getBoolean("stripUnnecessaryLocalsInRenderHelper", "rendering", "Strip unnecessary locals in RenderHelper::enableStandardItemLighting, no idea why it's there", true);

        quickerEnableUniversalBucketCheck = getBoolean("quickerEnableUniversalBucketCheck", "misc", "Optimizes FluidRegistry::enableUniversalBucket check", true);
        stripInstancedRandomFromSoundEventAccessor = getBoolean("stripInstancedRandomFromSoundEventAccessor", "misc", "Strips the boring instanced Random object from SoundEventAccessors and uses ThreadLocalRandom instead", true);
        classCaching = getBoolean("classCaching", "misc", "[W.I.P] - EXPERIMENTAL: Yet another attempt at caching classes between loads", false);
        copyScreenshotToClipboard = getBoolean("copyScreenshotToClipboard", "misc", "Copy image after screenshotting to clipboard", false);

        fixBlockIEBaseArrayIndexOutOfBoundsException = getBoolean("fixBlockIEBaseArrayIndexOutOfBoundsException", "modfixes", "When Immersive Engineering is installed, sometimes it or it's addons can induce an ArrayIndexOutOfBoundsException in BlockIEBase#getPushReaction. This option will be ignored when IE isn't installed", true);
        cleanupChickenASMClassHierarchyManager = getBoolean("cleanupChickenASMClassHierarchyManager", "modfixes", "EXPERIMENTAL: When ChickenASM (Library of CodeChickenLib and co.) is installed, ClassHierarchyManager can cache a lot of Strings and seem to be unused in any transformation purposes. This clears ClassHierarchyManager of those redundant strings. This option will be ignored when ChickenASM isn't installed", true);
        optimizeAmuletRelatedFunctions = getBoolean("optimizeAmuletRelatedFunctions", "modfixes", "Optimizes Astral Sorcery's Resplendent Prism related functions. This option will be ignored when Astral Sorcery isn't installed", true);
        labelCanonicalization = getBoolean("labelCanonicalization", "modfixes", "When Just Enough Items is installed, it deduplicates strings in the generated generalized suffix trees' edge labels. This option will be ignored when Just Enough Items isn't installed", true);
        skipCraftTweakerRecalculatingSearchTrees = getBoolean("skipCraftTweakerRecalculatingSearchTrees", "modfixes", "When CraftTweaker is installed, large modpacks tend to stall in the last stage of loading, when CraftTweaker inexplicably recalculates search trees. This option will be ignored when CraftTweaker isn't installed", true);
        bwmBlastingOilOptimization = getBoolean("bwmBlastingOilOptimization", "modfixes", "When Better with Mods is installed, optimize Blasting Oil related events. The original implementation harms server performance at any given moment. This option will be ignored when Better with Mods isn't installed", true);
        optimizeQMDBeamRenderer = getBoolean("optimizeQMDBeamRenderer", "modfixes", "When QMD is installed, optimize its BeamRenderer. The original implementation harms client performance heavily (takes ~5% of each tick time). This option will be ignored when QMD isn't installed", true);
        repairEvilCraftEIOCompat = getBoolean("repairEvilCraftEIOCompat", "modfixes", "When EvilCraft Compat + EnderIO is installed, repair the compatibility module", true);

        fixAmuletHolderCapability = getBoolean("fixAmuletHolderCapability", "capability", "Fixes Astral Sorcery applying AmuletHolderCapability to large amount of ItemStacks when it isn't needed. This option will be ignored when Astral Sorcery isn't installed", true);

        fixFillBucketEventNullPointerException = getBoolean("fixFillBucketEventNullPointerException", "forgefixes", "Fixes Forge's mistake of annotating FillBucketEvent#getFilledBucket as @Nonnull when the contract isn't fulfilled nor checked. First discovered here: https://github.com/Divine-Journey-2/main/issues/295", true);
        fixTileEntityOnLoadCME = getBoolean("fixTileEntityOnLoadCME", "forgefixes", "Fixes a vanilla-forge code interaction bug leading to a possible ConcurrentModificationException/StackOverflowError crash. First discovered here: https://github.com/GregTechCE/GregTech/issues/1256", true);
        removeForgeSecurityManager = getBoolean("removeForgeSecurityManager", "forgefixes", "EXPERIMENTAL: Forcibly remove Forge's FMLSecurityManager that adds very very slight overheads in calls that requires permission checks", false);
        fasterEntitySpawnPreparation = getBoolean("fasterEntitySpawnPreparation", "forgefixes", "Fixes Forge's EntityEntry calling a slow Constructor::newInstance call every time an entity spawns, it is replaced with a fast Function::get generated from LambdaMetafactory#metafactory", true);

        sparkProfileEntireGameLoad = getBoolean("sparkProfileEntireGameLoad", "spark", "When Spark is installed, profile the loading of the game in its entirety", false);
        sparkProfileCoreModLoading = getBoolean("sparkProfileCoreModLoading", "spark", "When Spark is installed, profile the loading of coremods, but only those that load after LoliASM", false);
        sparkProfileConstructionStage = getBoolean("sparkProfileConstructionStage", "spark", "When Spark is installed, profile the loading of FMLConstructionEvent stage", false);
        sparkProfilePreInitializationStage = getBoolean("sparkProfilePreInitializationStage", "spark", "When Spark is installed, profile the loading of FMLPreInitializationEvent stage", false);
        sparkProfileInitializationStage = getBoolean("sparkProfileInitializationStage", "spark", "When Spark is installed, profile the loading of FMLInitializationEvent stage", false);
        sparkProfilePostInitializationStage = getBoolean("sparkProfilePostInitializationStage", "spark", "When Spark is installed, profile the loading of FMLPostInitializationEvent stage", false);
        sparkProfileLoadCompleteStage = getBoolean("sparkProfileLoadCompleteStage", "spark", "When Spark is installed, profile the loading of FMLLoadCompleteEvent stage", false);
        includeAllThreadsWhenProfiling = getBoolean("includeAllThreadsWhenProfiling", "spark", "Allow LoliASM's Spark profiling to include all threads that are present", true);

        configuration.save();
    }

    public void editClassesThatCallBakedQuadCtor(Class<?> clazz) {
        Property prop = configuration.getCategory("bakedquad").get("classesThatCallBakedQuadCtor");
        Set<String> classes = new ObjectOpenHashSet<>(prop.getStringList());
        if (classes.add(clazz.getName())) {
            prop.set(classes.toArray(new String[0]));
            configuration.save();
            LoliLogger.instance.warn("{} added to classesThatCallBakedQuadCtor list in loliasm.cfg", clazz.getName());
        }
    }

    public void editClassesThatExtendBakedQuad(Class<?> clazz) {
        Property prop = configuration.getCategory("bakedquad").get("classesThatExtendBakedQuad");
        Set<String> classes = new ObjectOpenHashSet<>(prop.getStringList());
        if (classes.add(clazz.getName())) {
            prop.set(classes.toArray(new String[0]));
            configuration.save();
            LoliLogger.instance.warn("{} added to classesThatExtendBakedQuad list in loliasm.cfg", clazz.getName());
        }
    }

    private boolean setBoolean(String name, String category, boolean newValue) {
        Property prop = configuration.getCategory(category).get(name);
        prop.set(newValue);
        return newValue;
    }

    private String[] setStringArray(String name, String category, String... newValues) {
        Property prop = configuration.getCategory(category).get(name);
        prop.set(newValues);
        return newValues;
    }

    private boolean getBoolean(String name, String category, String description, boolean defaultValue) {
        Property prop = configuration.get(category, name, defaultValue);
        prop.setDefaultValue(defaultValue);
        prop.setComment(description + " - <default: " + defaultValue + ">");
        prop.setRequiresMcRestart(true);
        prop.setShowInGui(true);
        prop.setLanguageKey("loliasm.config." + name);
        return prop.getBoolean(defaultValue);
    }

    private String[] getStringArray(String name, String category, String description, String... defaultValue) {
        Property prop = configuration.get(category, name, defaultValue);
        prop.setDefaultValues(defaultValue);
        prop.setComment(description + " - <default: " + Arrays.toString(defaultValue) + ">");
        prop.setRequiresMcRestart(true);
        prop.setShowInGui(true);
        prop.setLanguageKey("loliasm.config." + name);
        return prop.getStringList();
    }

    @Deprecated
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

        @Ignore final String canonicalizeObjectsComment = "Experimental: Saves memory by pooling different Object instances and deduplicating them from different locations such as ResourceLocations, IBakedModels.";
        @Since("2.0") public final boolean canonicalizeObjects;

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
                    boolean canonicalizeObjects,
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
            this.canonicalizeObjects = canonicalizeObjects;
            this.optimizeDataStructures = optimizeDataStructures;
            this.optimizeFurnaceRecipes = optimizeFurnaceRecipes;
            this.optimizeBitsOfRendering = optimizeBitsOfRendering;
            this.miscOptimizations = miscOptimizations;
            this.modFixes = modFixes;
        }
    }

}

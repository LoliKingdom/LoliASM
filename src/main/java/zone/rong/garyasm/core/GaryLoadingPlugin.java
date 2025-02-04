package zone.rong.garyasm.core;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import zone.rong.garyasm.UnsafeGarys;
import zone.rong.garyasm.config.GaryConfig;
import zone.rong.garyasm.GaryLogger;
import zone.rong.garyasm.spark.GarySparker;
import zone.rong.garyasm.api.DeobfuscatingRewritePolicy;
import zone.rong.garyasm.api.StacktraceDeobfuscator;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@IFMLLoadingPlugin.Name("GaryASM")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class GaryLoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {

    public static final String VERSION = "5.23";

    public static final boolean isDeobf = FMLLaunchHandler.isDeobfuscatedEnvironment();

    static {
        if (!isDeobf && (GaryConfig.instance.sparkProfileCoreModLoading || GaryConfig.instance.sparkProfileEntireGameLoad)) {
            File modsFolder = new File(Launch.minecraftHome, "mods");
            for (File file : modsFolder.listFiles()) {
                if (file.isDirectory()) {
                    continue;
                }
                File toAddToCp = null;
                try (ZipFile jar = new ZipFile(file)) {
                    ZipEntry sparkProto = jar.getEntry("spark/spark.proto");
                    if (sparkProto != null) {
                        toAddToCp = file;
                    }
                } catch (IOException ignored) { }
                if (toAddToCp != null) {
                    try {
                        Launch.classLoader.addURL(toAddToCp.toURI().toURL());
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    // public static final boolean isModDirectorInstalled = GaryReflector.doesTweakExist("net.jan.moddirector.launchwrapper.ModDirectorTweaker");
    public static final boolean isVMOpenJ9 = SystemUtils.JAVA_VM_NAME.toLowerCase(Locale.ROOT).contains("openj9");
    public static final boolean isClient = FMLLaunchHandler.side() == Side.CLIENT;

    public GaryLoadingPlugin() {
        GaryLogger.instance.info("Garys are on the {}-side.", isClient ? "client" : "server");
        GaryLogger.instance.info("Garys are preparing and loading in mixins since Rongmario's too lazy to write pure ASM at times despite the mod being called 'GaryASM'");
        if (GaryConfig.instance.sparkProfileCoreModLoading) {
            GarySparker.start("coremod");
        }
        if (GaryConfig.instance.sparkProfileEntireGameLoad) {
            GarySparker.start("game");
        }
        if (GaryConfig.instance.outdatedCaCertsFix) {
            try (InputStream is = this.getClass().getResource("/cacerts").openStream()) {
                File cacertsCopy = File.createTempFile("cacerts", "");
                cacertsCopy.deleteOnExit();
                FileUtils.copyInputStreamToFile(is, cacertsCopy);
                System.setProperty("javax.net.ssl.trustStore", cacertsCopy.getAbsolutePath());
                GaryLogger.instance.warn("Replacing CA Certs with an updated one...");
            } catch (Exception e) {
                GaryLogger.instance.warn("Unable to replace CA Certs.", e);
            }
        }
        if (GaryConfig.instance.removeForgeSecurityManager) {
            UnsafeGarys.removeFMLSecurityManager();
        }
        if (GaryConfig.instance.crashReportImprovements || GaryConfig.instance.rewriteLoggingWithDeobfuscatedNames) {
            File modDir = new File(Launch.minecraftHome, "config/garyasm");
            modDir.mkdirs();
            // Initialize StacktraceDeobfuscator
            GaryLogger.instance.info("Initializing StacktraceDeobfuscator...");
            try {
                File mappings = new File(modDir, "methods-stable_39.csv");
                if (mappings.exists()) {
                    GaryLogger.instance.info("Found MCP stable-39 method mappings: {}", mappings.getName());
                } else {
                    GaryLogger.instance.info("Downloading MCP stable-39 method mappings to: {}", mappings.getName());
                }
                StacktraceDeobfuscator.init(mappings);
            } catch (Exception e) {
                GaryLogger.instance.error("Failed to get MCP stable-39 data!", e);
            }
            GaryLogger.instance.info("Initialized StacktraceDeobfuscator.");
            if (GaryConfig.instance.rewriteLoggingWithDeobfuscatedNames) {
                GaryLogger.instance.info("Installing DeobfuscatingRewritePolicy...");
                DeobfuscatingRewritePolicy.install();
                GaryLogger.instance.info("Installed DeobfuscatingRewritePolicy.");
            }
        }
        boolean needToDGSFFFF = isVMOpenJ9 && SystemUtils.IS_JAVA_1_8;
        int buildAppendIndex = SystemUtils.JAVA_VERSION.indexOf("_");
        if (needToDGSFFFF && buildAppendIndex != -1) {
            if (Integer.parseInt(SystemUtils.JAVA_VERSION.substring(buildAppendIndex + 1)) < 265) {
                for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                    if (arg.equals("-Xjit:disableGuardedStaticFinalFieldFolding")) {
                        needToDGSFFFF = false;
                        break;
                    }
                }
                if (needToDGSFFFF) {
                    GaryLogger.instance.fatal("GaryASM notices that you're using Eclipse OpenJ9 {}!", SystemUtils.JAVA_VERSION);
                    GaryLogger.instance.fatal("This OpenJ9 version is outdated and contains a critical bug: https://github.com/eclipse-openj9/openj9/issues/8353");
                    GaryLogger.instance.fatal("Either use '-Xjit:disableGuardedStaticFinalFieldFolding' as part of your java arguments, or update OpenJ9!");
                }
            }
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
        return "zone.rong.garyasm.core.GaryFMLCallHook";
    }

    @Override
    public void injectData(Map<String, Object> data) { }

    @Override
    public String getAccessTransformerClass() {
        return "zone.rong.garyasm.core.GaryTransformer";
    }

    @Override
    public List<String> getMixinConfigs() {
        return isClient ? Arrays.asList(
                "mixins.devenv.json",
                "mixins.internal.json",
                "mixins.vanities.json",
                "mixins.registries.json",
                "mixins.stripitemstack.json",
                "mixins.lockcode.json",
                "mixins.recipes.json",
                "mixins.misc_fluidregistry.json",
                "mixins.forgefixes.json",
                "mixins.capability.json",
                "mixins.singletonevents.json",
                "mixins.efficienthashing.json",
                "mixins.crashes.json",
                "mixins.fix_mc129057.json",
                "mixins.bucket.json",
                "mixins.priorities.json",
                "mixins.rendering.json",
                "mixins.datastructures_modelmanager.json",
                "mixins.screenshot.json",
                "mixins.ondemand_sprites.json",
                "mixins.searchtree_vanilla.json",
                "mixins.resolve_mc2071.json",
                "mixins.fix_mc_skindownloading.json",
                "mixins.fix_mc186052.json") :
                Arrays.asList(
                        "mixins.devenv.json",
                        "mixins.vfix_bugfixes.json",
                        "mixins.internal.json",
                        "mixins.vanities.json",
                        "mixins.registries.json",
                        "mixins.stripitemstack.json",
                        "mixins.lockcode.json",
                        "mixins.recipes.json",
                        "mixins.misc_fluidregistry.json",
                        "mixins.forgefixes.json",
                        "mixins.capability.json",
                        "mixins.singletonevents.json",
                        "mixins.efficienthashing.json",
                        "mixins.priorities.json",
                        "mixins.crashes.json",
                        "mixins.fix_mc129057.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        if (FMLLaunchHandler.isDeobfuscatedEnvironment() && "mixins.devenv.json".equals(mixinConfig)) {
            return true;
        }
        if (isClient) {
            switch (mixinConfig) {
                case "mixins.bucket.json":
                    return GaryConfig.instance.reuseBucketQuads;
                case "mixins.rendering.json":
                    return GaryConfig.instance.optimizeSomeRendering;
                case "mixins.datastructures_modelmanager.json":
                    return GaryConfig.instance.moreModelManagerCleanup;
                case "mixins.screenshot.json":
                    return GaryConfig.instance.releaseScreenshotCache || GaryConfig.instance.asyncScreenshot;
                case "mixins.resolve_mc2071.json":
                    return GaryConfig.instance.resolveMC2071;
                case "mixins.fix_mc_skindownloading.json":
                    return GaryConfig.instance.limitSkinDownloadingThreads;
            }
        }
        switch (mixinConfig) {
            case "mixins.registries.json":
                return GaryConfig.instance.optimizeRegistries;
            case "mixins.stripitemstack.json":
                return GaryConfig.instance.stripNearUselessItemStackFields;
            case "mixins.lockcode.json":
                return GaryConfig.instance.lockCodeCanonicalization;
            case "mixins.recipes.json":
                return GaryConfig.instance.optimizeFurnaceRecipeStore;
            case "mixins.misc_fluidregistry.json":
                return GaryConfig.instance.quickerEnableUniversalBucketCheck;
            case "mixins.forgefixes.json":
                return GaryConfig.instance.fixFillBucketEventNullPointerException || GaryConfig.instance.fixTileEntityOnLoadCME;
            case "mixins.capability.json":
                return GaryConfig.instance.delayItemStackCapabilityInit;
            case "mixins.singletonevents.json":
                return GaryConfig.instance.makeEventsSingletons;
            case "mixins.efficienthashing.json":
                return GaryConfig.instance.efficientHashing;
            case "mixins.crashes.json":
                return GaryConfig.instance.crashReportImprovements;
            case "mixins.fix_mc129057.json":
                return GaryConfig.instance.fixMC129057;
            case "mixins.priorities.json":
                return GaryConfig.instance.threadPriorityFix;
        }
        return true;
    }

}
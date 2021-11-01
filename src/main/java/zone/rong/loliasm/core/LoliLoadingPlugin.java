package zone.rong.loliasm.core;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.SystemUtils;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import zone.rong.loliasm.UnsafeLolis;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.LoliReflector;
import zone.rong.loliasm.spark.LoliSparker;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@IFMLLoadingPlugin.Name("LoliASM")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class LoliLoadingPlugin implements IFMLLoadingPlugin {

    public static final String VERSION = "4.2.4";

    public static final boolean isDeobf = FMLLaunchHandler.isDeobfuscatedEnvironment();

    static {
        if (!isDeobf && (LoliConfig.instance.sparkProfileCoreModLoading || LoliConfig.instance.sparkProfileEntireGameLoad)) {
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

    public static final boolean isOptifineInstalled = LoliReflector.doesClassExist("optifine.OptiFineForgeTweaker");
    public static final boolean isVMOpenJ9 = SystemUtils.JAVA_VM_NAME.toLowerCase(Locale.ROOT).contains("openj9");
    public static final boolean isClient = FMLLaunchHandler.side() == Side.CLIENT;

    public static final boolean squashBakedQuads = LoliConfig.instance.squashBakedQuads && !isOptifineInstalled;

    public LoliLoadingPlugin() {
        LoliLogger.instance.info("Lolis are on the {}-side.", isClient ? "client" : "server");
        LoliLogger.instance.info("Lolis are preparing and loading in mixins since Rongmario's too lazy to write pure ASM at times despite the mod being called 'LoliASM'");
        if (LoliConfig.instance.sparkProfileCoreModLoading) {
            LoliSparker.start("coremod");
        }
        if (LoliConfig.instance.sparkProfileEntireGameLoad) {
            LoliSparker.start("game");
        }
        if (LoliConfig.instance.removeForgeSecurityManager) {
            UnsafeLolis.removeFMLSecurityManager();
        }
        MixinBootstrap.init();
        boolean needToDGSFFFF = isVMOpenJ9 && SystemUtils.IS_JAVA_1_8;
        int buildAppendIndex = SystemUtils.JAVA_VERSION.indexOf("_");
        if (needToDGSFFFF && buildAppendIndex != -1) {
            needToDGSFFFF = Integer.parseInt(SystemUtils.JAVA_VERSION.substring(buildAppendIndex + 1)) < 265;
        }
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (needToDGSFFFF && arg.equals("-Xjit:disableGuardedStaticFinalFieldFolding")) {
                needToDGSFFFF = false;
                break;
            }
        }
        if (needToDGSFFFF) {
            LoliLogger.instance.fatal("LoliASM notices that you're using Eclipse OpenJ9 {} which is outdated and contains a critical bug: {} that slows the game down a lot. Either append -Xjit:disableGuardedStaticFinalFieldFolding to your java arguments or update your Java!", SystemUtils.JAVA_VERSION, "https://github.com/eclipse-openj9/openj9/issues/8353");
        }
        Mixins.addConfiguration("mixins.internal.json");
        Mixins.addConfiguration("mixins.vanities.json");
        if (LoliConfig.instance.optimizeRegistries) {
            Mixins.addConfiguration("mixins.registries.json");
        }
        if (LoliConfig.instance.stripNearUselessItemStackFields) {
            Mixins.addConfiguration("mixins.stripitemstack.json");
        }
        if (LoliConfig.instance.lockCodeCanonicalization) {
            Mixins.addConfiguration("mixins.lockcode.json");
        }
        if (LoliConfig.instance.optimizeFurnaceRecipeStore) {
            Mixins.addConfiguration("mixins.recipes.json");
        }
        if (LoliConfig.instance.quickerEnableUniversalBucketCheck) {
            Mixins.addConfiguration("mixins.misc_fluidregistry.json");
        }
        if (LoliConfig.instance.fixFillBucketEventNullPointerException || LoliConfig.instance.fixTileEntityOnLoadCME) {
            Mixins.addConfiguration("mixins.forgefixes.json");
        }
        if (isClient) {
            if (LoliConfig.instance.reuseBucketQuads) {
                Mixins.addConfiguration("mixins.bucket.json");
            }
            if (LoliConfig.instance.optimizeSomeRendering) {
                Mixins.addConfiguration("mixins.rendering.json");
            }
            if (LoliConfig.instance.moreModelManagerCleanup) {
                Mixins.addConfiguration("mixins.datastructures_modelmanager.json");
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
        return "zone.rong.loliasm.core.LoliFMLCallHook";
    }

    @Override
    public void injectData(Map<String, Object> data) { }

    @Override
    public String getAccessTransformerClass() {
        return "zone.rong.loliasm.core.LoliTransformer";
    }
}
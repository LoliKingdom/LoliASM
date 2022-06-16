package zone.rong.loliasm.common.crashes.mixins;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.crash.CrashReport;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.MinecraftError;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.SplashProgress;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.LoliASM;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.common.crashes.*;
import zone.rong.loliasm.common.crashes.CrashUtils;
import zone.rong.loliasm.config.LoliConfig;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.FutureTask;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements IMinecraftExtender {

    @Shadow public static byte[] memoryReserve;

    @Shadow @Final private static Logger LOGGER;

    @Shadow public static long getSystemTime() {
        throw new AssertionError();
    }

    @Shadow @Final private List<IResourcePack> defaultResourcePacks;
    @Shadow @Final private MetadataSerializer metadataSerializer;
    @Shadow @Final private Queue<FutureTask<?>> scheduledTasks;

    @Shadow public TextureManager renderEngine;
    @Shadow public GameSettings gameSettings;
    @Shadow public FontRenderer fontRenderer;
    @Shadow public EntityRenderer entityRenderer;
    @Shadow @Nullable public GuiScreen currentScreen;
    @Shadow public int displayWidth;
    @Shadow public int displayHeight;
    @Shadow public GuiIngame ingameGUI;
    @Shadow volatile boolean running;
    @Shadow private boolean hasCrashed;
    @Shadow private CrashReport crashReporter;
    @Shadow private SoundHandler soundHandler;
    @Shadow private LanguageManager languageManager;
    @Shadow private IReloadableResourceManager resourceManager;
    @Shadow private int leftClickCounter;
    @Shadow private Framebuffer framebuffer;
    @Shadow private long debugCrashKeyPressTime;

    @Shadow public abstract void shutdownMinecraftApplet();
    @Shadow public abstract CrashReport addGraphicsAndWorldToCrashReport(CrashReport theCrash);
    @Shadow public abstract void updateDisplay();
    @Shadow public abstract void displayGuiScreen(@Nullable GuiScreen guiScreenIn);
    @Shadow public abstract void loadWorld(@Nullable WorldClient worldClientIn);
    @Shadow @Deprecated public abstract void refreshResources();
    @Shadow @Nullable public abstract NetHandlerPlayClient getConnection();

    @Shadow protected abstract void init() throws LWJGLException, IOException;
    @Shadow protected abstract void runGameLoop() throws IOException;
    @Shadow protected abstract void checkGLError(String message);

    @Shadow private boolean actionKeyF3;
    @Shadow private boolean integratedServerIsRunning;
    @Shadow @Nullable private IntegratedServer integratedServer;

    @Shadow public abstract GuiToast getToastGui();

    @Shadow public abstract ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule);

    @Unique private boolean crashIntegratedServerNextTick;
    @Unique private int clientCrashCount = 0;
    @Unique private int serverCrashCount = 0;
    @Unique @Nullable private Class<?> customMainMenuGuiCustomClass = null;

    private static void resetGlStates() {
        // Clear matrix stack
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_COLOR);
        GlStateManager.loadIdentity();

        // Clear attribute stacks TODO: Broken, a stack underflow breaks LWJGL
        // try {
        //     do GL11.glPopAttrib(); while (GlStateManager.glGetError() == 0);
        // } catch (Throwable ignored) {}
        //
        // try {
        //     do GL11.glPopClientAttrib(); while (GlStateManager.glGetError() == 0);
        // } catch (Throwable ignored) {}

        // Reset texture
        GlStateManager.bindTexture(0);
        GlStateManager.disableTexture2D();

        // Reset GL lighting
        GlStateManager.disableLighting();
        GlStateManager.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, RenderHelper.setColorBuffer(0.2F, 0.2F, 0.2F, 1.0F));
        for (int i = 0; i < 8; ++i) {
            GlStateManager.disableLight(i);
            GlStateManager.glLight(GL11.GL_LIGHT0 + i, GL11.GL_AMBIENT, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GlStateManager.glLight(GL11.GL_LIGHT0 + i, GL11.GL_POSITION, RenderHelper.setColorBuffer(0.0F, 0.0F, 1.0F, 0.0F));

            if (i == 0) {
                GlStateManager.glLight(GL11.GL_LIGHT0 + i, GL11.GL_DIFFUSE, RenderHelper.setColorBuffer(1.0F, 1.0F, 1.0F, 1.0F));
                GlStateManager.glLight(GL11.GL_LIGHT0 + i, GL11.GL_SPECULAR, RenderHelper.setColorBuffer(1.0F, 1.0F, 1.0F, 1.0F));
            } else {
                GlStateManager.glLight(GL11.GL_LIGHT0 + i, GL11.GL_DIFFUSE, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
                GlStateManager.glLight(GL11.GL_LIGHT0 + i, GL11.GL_SPECULAR, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            }
        }
        GlStateManager.disableColorMaterial();
        GlStateManager.colorMaterial(1032, 5634);

        // Reset depth
        GlStateManager.disableDepth();
        GlStateManager.depthFunc(513);
        GlStateManager.depthMask(true);

        // Reset blend mode
        GlStateManager.disableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glBlendEquation(GL14.GL_FUNC_ADD);

        // Reset fog
        GlStateManager.disableFog();
        GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
        GlStateManager.setFogDensity(1.0F);
        GlStateManager.setFogStart(0.0F);
        GlStateManager.setFogEnd(1.0F);
        GlStateManager.glFog(GL11.GL_FOG_COLOR, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 0.0F));
        if (GLContext.getCapabilities().GL_NV_fog_distance) GlStateManager.glFogi(GL11.GL_FOG_MODE, 34140);

        // Reset polygon offset
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();

        // Reset color logic
        GlStateManager.disableColorLogic();
        GlStateManager.colorLogicOp(5379);

        // Reset texgen TODO: is this correct?
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.S);
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.T);
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.R);
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.Q);
        GlStateManager.texGen(GlStateManager.TexGen.S, 9216);
        GlStateManager.texGen(GlStateManager.TexGen.T, 9216);
        GlStateManager.texGen(GlStateManager.TexGen.R, 9216);
        GlStateManager.texGen(GlStateManager.TexGen.Q, 9216);
        GlStateManager.texGen(GlStateManager.TexGen.S, 9474, RenderHelper.setColorBuffer(1.0F, 0.0F, 0.0F, 0.0F));
        GlStateManager.texGen(GlStateManager.TexGen.T, 9474, RenderHelper.setColorBuffer(0.0F, 1.0F, 0.0F, 0.0F));
        GlStateManager.texGen(GlStateManager.TexGen.R, 9474, RenderHelper.setColorBuffer(0.0F, 0.0F, 1.0F, 0.0F));
        GlStateManager.texGen(GlStateManager.TexGen.Q, 9474, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        GlStateManager.texGen(GlStateManager.TexGen.S, 9217, RenderHelper.setColorBuffer(1.0F, 0.0F, 0.0F, 0.0F));
        GlStateManager.texGen(GlStateManager.TexGen.T, 9217, RenderHelper.setColorBuffer(0.0F, 1.0F, 0.0F, 0.0F));
        GlStateManager.texGen(GlStateManager.TexGen.R, 9217, RenderHelper.setColorBuffer(0.0F, 0.0F, 1.0F, 0.0F));
        GlStateManager.texGen(GlStateManager.TexGen.Q, 9217, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));

        // Disable lightmap
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();

        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        // Reset texture parameters
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 1000);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, 1000);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, -1000);
        GlStateManager.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0F);

        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        GlStateManager.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 0.0F));
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE);
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE);
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL15.GL_SRC0_RGB, GL11.GL_TEXTURE);
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL15.GL_SRC1_RGB, GL13.GL_PREVIOUS);
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL15.GL_SRC2_RGB, GL13.GL_CONSTANT);
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL15.GL_SRC0_ALPHA, GL11.GL_TEXTURE);
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL15.GL_SRC1_ALPHA, GL13.GL_PREVIOUS);
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL15.GL_SRC2_ALPHA, GL13.GL_CONSTANT);
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR);
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND1_RGB, GL11.GL_SRC_COLOR);
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND2_RGB, GL11.GL_SRC_ALPHA);
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA);
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND1_ALPHA, GL11.GL_SRC_ALPHA);
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND2_ALPHA, GL11.GL_SRC_ALPHA);
        GlStateManager.glTexEnvf(GL11.GL_TEXTURE_ENV, GL13.GL_RGB_SCALE, 1.0F);
        GlStateManager.glTexEnvf(GL11.GL_TEXTURE_ENV, GL11.GL_ALPHA_SCALE, 1.0F);

        GlStateManager.disableNormalize();
        GlStateManager.shadeModel(7425);
        GlStateManager.disableRescaleNormal();
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.clearDepth(1.0D);
        GlStateManager.glLineWidth(1.0F);
        GlStateManager.glNormal3f(0.0F, 0.0F, 1.0F);
        GlStateManager.glPolygonMode(GL11.GL_FRONT, GL11.GL_FILL);
        GlStateManager.glPolygonMode(GL11.GL_BACK, GL11.GL_FILL);

        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(7425);
        GlStateManager.clearDepth(1.0D);
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(515);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
    }

    @Override
    public boolean shouldCrashIntegratedServerNextTick() {
        return crashIntegratedServerNextTick;
    }

    @Override
    public void showWarningScreen(CrashReport report) {
        // TODO: runGuiLoop instead, to prevent errors from happening while the warning screen is open?
        addScheduledTask(() -> displayGuiScreen(new GuiWarningScreen(report, currentScreen)));
    }

    @Override
    public void makeErrorNotification(CrashReport report) {
        if (!LoliConfig.instance.hideToastsAndContinuePlaying) {
            ProblemToast lastToast = getToastGui().getToast(ProblemToast.class, IToast.NO_TOKEN);
            if (lastToast != null) {
                lastToast.hide = true;
            }
            getToastGui().add(new ProblemToast(report));
        }
    }

    /**
     * @author VanillaFix + Rongmario
     * @reason Allows playing after crashing, TODO: not making this an Overwrite.
     */
    @Overwrite
    public void run() {
        this.running = true;
        try {
            this.init();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Initializing game");
            displayInitErrorScreen(addGraphicsAndWorldToCrashReport(crashreport));
            // crashreport.makeCategory("Initialization");
            // this.displayCrashReport(this.addGraphicsAndWorldToCrashReport(crashreport));
            return;
        }
        while (true) {
            try {
                while (this.running) {
                    if (!this.hasCrashed || this.crashReporter == null) {
                        try {
                            this.runGameLoop();
                        } catch (ReportedException e) {
                            clientCrashCount++;
                            addGraphicsAndWorldToCrashReport(e.getCrashReport());
                            addInfoToCrash(e.getCrashReport());
                            resetGameState();
                            LOGGER.fatal("Reported exception thrown!", e);
                            displayCrashScreen(e.getCrashReport());
                        } catch (Throwable e) {
                            clientCrashCount++;
                            CrashReport report = new CrashReport("Unexpected error", e);
                            addGraphicsAndWorldToCrashReport(report);
                            addInfoToCrash(report);
                            resetGameState();
                            LOGGER.fatal("Unreported exception thrown!", e);
                            displayCrashScreen(report);
                        }
                        /*
                        } catch (OutOfMemoryError var10) {
                            this.freeMemory();
                            this.displayGuiScreen(new GuiMemoryErrorScreen());
                            System.gc();
                        }
                         */
                    } else {
                        // this.displayCrashReport(this.crashReporter);
                        serverCrashCount++;
                        addInfoToCrash(crashReporter);
                        freeMemory();
                        displayCrashScreen(crashReporter);
                        hasCrashed = false;
                    }
                }
            } catch (MinecraftError mcError) {
                // break;
                // ignored
            // }
            /*
            catch (ReportedException reportedexception)
            {
                this.addGraphicsAndWorldToCrashReport(reportedexception.getCrashReport());
                this.freeMemory();
                LOGGER.fatal("Reported exception thrown!", (Throwable)reportedexception);
                this.displayCrashReport(reportedexception.getCrashReport());
                break;
            }
            catch (Throwable throwable1)
            {
                CrashReport crashreport1 = this.addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", throwable1));
                this.freeMemory();
                LOGGER.fatal("Unreported exception thrown!", throwable1);
                this.displayCrashReport(crashreport1);
                break;
            }
             */
            } finally {
                this.shutdownMinecraftApplet();
            }
            return;
        }
    }

    /**
     * @author VanillaFix
     * @reason Display custom report
     */
    @Overwrite
    public void displayCrashReport(CrashReport report) {
        CrashUtils.outputReport(report);
    }

    /**
     * @author VanillaFix
     * @reason Disconnect from the current world and free memory, using a memory reserve
     * to make sure that an OutOfMemory doesn't happen while doing this.
     * <p>
     * - Fixes MC-128953: Memory reserve not recreated after out-of memory
     */
    @Overwrite
    public void freeMemory() {
        resetGameState();
    }

    /**
     * @reason Replaces the vanilla F3 + C logic to immediately crash rather than requiring
     * that the buttons are pressed for 6 seconds and add more crash types:
     * F3 + C - Client crash
     * Alt + F3 + C - Integrated server crash
     * Shift + F3 + C - Scheduled client task exception
     * Alt + Shift + F3 + C - Scheduled server task exception
     * <p>
     * Note: Left Shift + F3 + C doesn't work on most keyboards, see http://keyboardchecker.com/
     * Use the right shift instead.
     * <p>
     * TODO: Make this work outside the game too (for example on the main menu).
     */
    @Redirect(method = "runTickKeyboard", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;debugCrashKeyPressTime:J", ordinal = 0))
    private long checkForF3C(Minecraft mc) {
        // Fix: Check if keys are down before checking time pressed
        if (Keyboard.isKeyDown(Keyboard.KEY_F3) && Keyboard.isKeyDown(Keyboard.KEY_C)) {
            debugCrashKeyPressTime = getSystemTime();
            actionKeyF3 = true;
        } else {
            debugCrashKeyPressTime = -1L;
        }
        if (debugCrashKeyPressTime > 0L) {
            if (getSystemTime() - debugCrashKeyPressTime >= 0) {
                if (GuiScreen.isShiftKeyDown()) {
                    if (GuiScreen.isAltKeyDown()) {
                        if (integratedServerIsRunning) {
                            integratedServer.addScheduledTask(() -> {
                                throw new ReportedException(new CrashReport("Manually triggered server-side scheduled task exception", new Throwable()));
                            });
                        }
                    } else {
                        scheduledTasks.add(ListenableFutureTask.create(() -> {
                            throw new ReportedException(new CrashReport("Manually triggered client-side scheduled task exception", new Throwable()));
                        }));
                    }
                } else {
                    if (GuiScreen.isAltKeyDown()) {
                        if (integratedServerIsRunning) {
                            crashIntegratedServerNextTick = true;
                        }
                    } else {
                        throw new ReportedException(new CrashReport("Manually triggered client-side debug crash", new Throwable()));
                    }
                }
            }
        }
        return -1;
    }

    /**
     * @reason Disables the vanilla F3 + C logic.
     */
    @Redirect(method = "runTickKeyboard", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;isKeyDown(I)Z", ordinal = 0))
    private boolean isKeyDownF3(int key) {
        return false;
    }

    /**
     * @reason Checks if Ctrl + I is pressed and opens a warning screen if there is a visible or queued error notification.
     * TODO: Main menu too
     */
    @Inject(method = "runTickKeyboard", at = @At("HEAD"))
    private void checkForCtrlI(CallbackInfo ci) {
        if (GuiScreen.isCtrlKeyDown() && !GuiScreen.isShiftKeyDown() && !GuiScreen.isAltKeyDown() && Keyboard.isKeyDown(Keyboard.KEY_I)) {
            ProblemToast lastToast = getToastGui().getToast(ProblemToast.class, IToast.NO_TOKEN);
            if (lastToast != null) {
                lastToast.hide = true;
                displayGuiScreen(new GuiWarningScreen(lastToast.report, currentScreen));
            }
        }
    }

    private void resetGameState() {
        try {
            // Free up memory such that this works properly in case of an OutOfMemoryError
            int originalMemoryReserveSize = -1;
            try { // In case another mod actually deletes the memoryReserve field
                if (memoryReserve != null) {
                    originalMemoryReserveSize = memoryReserve.length;
                    memoryReserve = new byte[0];
                }
            } catch (Throwable ignored) {}
            // Reset registered resettables
            IStateful.resetAll();
            // Close the world
            if (getConnection() != null) {
                // Fix: Close the connection to avoid receiving packets from old server
                // when playing in another world (MC-128953)
                getConnection().getNetworkManager().closeChannel(new TextComponentString("[VanillaFix] Client crashed"));
            }
            loadWorld(null);
            if (entityRenderer.isShaderActive()) {
                entityRenderer.stopUseShader();
            }
            scheduledTasks.clear(); // TODO: Figure out why this isn't necessary for vanilla disconnect
            // Reset graphics
            resetGlStates();
            // Re-create memory reserve so that future crashes work well too
            if (originalMemoryReserveSize != -1) {
                try {
                    memoryReserve = new byte[originalMemoryReserveSize];
                } catch (Throwable ignored) { }
            }
            System.gc();
        } catch (Throwable t) {
            LOGGER.error("Failed to reset state after a crash", t);
            try {
                IStateful.resetAll();
                resetGlStates();
            } catch (Throwable ignored) { }
        }
    }

    private void addInfoToCrash(CrashReport report) {
        report.getCategory().addDetail("Client Crashes Since Restart", () -> String.valueOf(clientCrashCount));
        report.getCategory().addDetail("Integrated Server Crashes Since Restart", () -> String.valueOf(serverCrashCount));
    }

    private void displayInitErrorScreen(CrashReport report) {
        CrashUtils.outputReport(report);
        try {
            try {
                URL url = LoliASM.class.getProtectionDomain().getCodeSource().getLocation();
                if (url.getProtocol().equals("jar")) {
                    url = new URL(url.getFile().substring(0, url.getFile().indexOf('!')));
                }
                File modFile = new File(url.toURI());
                defaultResourcePacks.add(modFile.isDirectory() ? new FolderResourcePack(modFile) : new FileResourcePack(modFile));
            } catch (Throwable t) {
                LoliLogger.instance.error("Failed to load LoliASM resource pack", t);
            }
            resourceManager = new SimpleReloadableResourceManager(metadataSerializer);
            renderEngine = new TextureManager(resourceManager);
            resourceManager.registerReloadListener(renderEngine);

            languageManager = new LanguageManager(metadataSerializer, gameSettings.language);
            resourceManager.registerReloadListener(languageManager);

            refreshResources(); // TODO: Why is this necessary?

            fontRenderer = new FontRenderer(gameSettings, new ResourceLocation("textures/font/ascii.png"), renderEngine, false);
            resourceManager.registerReloadListener(fontRenderer);

            soundHandler = new SoundHandler(resourceManager, gameSettings);
            resourceManager.registerReloadListener(soundHandler);

            running = true;
            try {
                SplashProgress.pause();// Disable the forge splash progress screen
            } catch (Throwable ignored) { }
            runGUILoop(new GuiInitErrorScreen(report));
        } catch (Throwable t) {
            LOGGER.error("An uncaught exception occured while displaying the init error screen, making normal report instead", t);
            displayCrashReport(report);
            System.exit(report.getFile() != null ? -1 : -2);
        }
    }

    private void displayCrashScreen(CrashReport report) {
        try {
            CrashUtils.outputReport(report);
            // Reset hasCrashed, debugCrashKeyPressTime, and crashIntegratedServerNextTick
            hasCrashed = false;
            debugCrashKeyPressTime = -1;
            crashIntegratedServerNextTick = false;
            // Vanilla does this when switching to main menu but not our custom crash screen
            // nor the out of memory screen (see https://bugs.mojang.com/browse/MC-128953)
            gameSettings.showDebugInfo = false;
            ingameGUI.getChatGUI().clearChatMessages(true);
            // Display the crash screen
            runGUILoop(new GuiCrashScreen(report));
        } catch (Throwable t) {
            // The crash screen has crashed. Report it normally instead.
            LOGGER.error("An uncaught exception occured while displaying the crash screen, making normal report instead", t);
            displayCrashReport(report);
            System.exit(report.getFile() != null ? -1 : -2);
        }
    }

    private void runGUILoop(GuiScreen screen) throws IOException {
        displayGuiScreen(screen);
        while (running && currentScreen != null && !(currentScreen instanceof GuiMainMenu) && !(isCurrentMenuCustom(currentScreen))) {
            if (Display.isCreated() && Display.isCloseRequested()) {
                System.exit(0);
            }
            leftClickCounter = 10000;
            currentScreen.handleInput();
            currentScreen.updateScreen();

            GlStateManager.pushMatrix();
            GlStateManager.clear(16640);
            framebuffer.bindFramebuffer(true);
            GlStateManager.enableTexture2D();

            GlStateManager.viewport(0, 0, displayWidth, displayHeight);

            // EntityRenderer.setupOverlayRendering
            ScaledResolution scaledResolution = new ScaledResolution((Minecraft) (Object) this);
            GlStateManager.clear(256);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0D, scaledResolution.getScaledWidth_double(), scaledResolution.getScaledHeight_double(), 0, 1000, 3000);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0, 0, -2000);
            GlStateManager.clear(256);

            int width = scaledResolution.getScaledWidth();
            int height = scaledResolution.getScaledHeight();
            int mouseX = Mouse.getX() * width / displayWidth;
            int mouseY = height - Mouse.getY() * height / displayHeight - 1;
            currentScreen.drawScreen(mouseX, mouseY, 0);

            framebuffer.unbindFramebuffer();
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            framebuffer.framebufferRender(displayWidth, displayHeight);
            GlStateManager.popMatrix();

            updateDisplay();
            Thread.yield();
            Display.sync(60);
            checkGLError("LoliASM GUI Loop");
        }
    }

    private boolean isCurrentMenuCustom(GuiScreen currentScreen) {
        if (customMainMenuGuiCustomClass == null) {
            try {
                customMainMenuGuiCustomClass = Class.forName("lumien.custommainmenu.gui.GuiCustom");
            } catch (ClassNotFoundException e) {
                customMainMenuGuiCustomClass = Object.class;
            }
        }
        return customMainMenuGuiCustomClass != Object.class && customMainMenuGuiCustomClass.isAssignableFrom(currentScreen.getClass());
    }

}

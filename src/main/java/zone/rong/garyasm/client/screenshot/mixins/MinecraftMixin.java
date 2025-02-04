package zone.rong.garyasm.client.screenshot.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ScreenshotEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import zone.rong.garyasm.client.screenshot.ScreenshotThread;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ScreenShotHelper;saveScreenshot(Ljava/io/File;IILnet/minecraft/client/shader/Framebuffer;)Lnet/minecraft/util/text/ITextComponent;", ordinal = 0))
    private ITextComponent runAsyncScreenshotProcessing(File gameDir, int width, int height, Framebuffer buffer) {
        try {
            BufferedImage screenshot = ScreenShotHelper.createScreenshot(width, height, buffer);
            File screenshotDir = new File(gameDir, "screenshots");
            screenshotDir.mkdir();
            File screenshotFile = ScreenShotHelperInvoker.invokeGetTimestampedPNGFileForDirectory(screenshotDir).getCanonicalFile();
            ScreenshotEvent event = ForgeHooksClient.onScreenshot(screenshot, screenshotFile);
            if (event.isCanceled()) {
                return event.getCancelMessage();
            }
            new ScreenshotThread(event.getScreenshotFile(), screenshot, event.getResultMessage()).start();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return new TextComponentTranslation("screenshot.failure", e.getMessage());
        }
    }

    @Redirect(method = "dispatchKeypresses", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;printChatMessage(Lnet/minecraft/util/text/ITextComponent;)V"))
    private void redirectPrintScreenshotPathInChat(GuiNewChat chat, ITextComponent text) { }

}

package zone.rong.blahajasm.client.screenshot;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import zone.rong.blahajasm.BlahajLogger;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ScreenshotThread extends Thread {

    public ScreenshotThread(File screenshotFile, BufferedImage screenshot, @Nullable ITextComponent eventResultMessage) {
        super(() -> {
            Minecraft mc = Minecraft.getMinecraft();
            try {
                ImageIO.write(screenshot, "png", screenshotFile);
                if (eventResultMessage != null) {
                    mc.addScheduledTask(() -> mc.ingameGUI.getChatGUI().printChatMessage(eventResultMessage));
                    return;
                }
                ITextComponent fileName = new TextComponentString(screenshotFile.getName());
                fileName.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, screenshotFile.getAbsolutePath()));
                fileName.getStyle().setUnderlined(true);
                mc.addScheduledTask(() -> mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("screenshot.success", fileName)));
            } catch (Throwable t) {
                BlahajLogger.instance.warn("Couldn't save screenshot", t);
                mc.addScheduledTask(() -> mc.ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("screenshot.failure", t.getMessage())));
            }
        }, "BlahajScreenshotThread");
    }

}

package zone.rong.loliasm.common.crashes;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiWarningScreen extends GuiProblemScreen {

    public GuiWarningScreen(CrashReport report, GuiScreen nextScreen) {
        super(report);
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButton(0, width / 2 - 155,  height / 4 + 120 + 12, 150, 20, I18n.format("loliasm.gui.keepPlaying")));
        // TODO: Pause sounds too (see Minecraft.displayInGameMenu)
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        if (button.id == 0) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) { // TODO: localize number of lines
        drawDefaultBackground();
        drawCenteredString(fontRenderer, I18n.format("loliasm.warnscreen.title"), width / 2, height / 4 - 40, 0xFFFFFF);

        int textColor = 0xD0D0D0;
        int x = width / 2 - 155;
        int y = height / 4;

        y -= 20;
        drawString(fontRenderer, I18n.format("loliasm.warnscreen.summary"), x, y, textColor);
        drawString(fontRenderer, I18n.format("loliasm.warnscreen.paragraph1.line1"), x, y += 18, textColor);
        drawString(fontRenderer, I18n.format("loliasm.warnscreen.paragraph1.line2"), x, y += 9, textColor);
        drawString(fontRenderer, I18n.format("loliasm.warnscreen.paragraph1.line3"), x, y += 9, textColor);

        drawCenteredString(fontRenderer, getModListString(), width / 2, y += 11, 0xE0E000);

        drawString(fontRenderer, I18n.format("loliasm.crashscreen.paragraph2.line1"), x, y += 11, textColor);
        drawString(fontRenderer, I18n.format("loliasm.crashscreen.paragraph2.line2"), x, y += 9, textColor);

        drawCenteredString(fontRenderer, report.getFile() != null ?
                "\u00A7n" + report.getFile().getName() :
                I18n.format("loliasm.crashscreen.reportSaveFailed"), width / 2, y += 11, 0x00FF00);

        drawString(fontRenderer, I18n.format("loliasm.warnscreen.paragraph3.line1"), x, y += 12, textColor);
        drawString(fontRenderer, I18n.format("loliasm.warnscreen.paragraph3.line2"), x, y += 9, textColor);
        drawString(fontRenderer, I18n.format("loliasm.warnscreen.paragraph3.line3"), x, y += 9, textColor);
        drawString(fontRenderer, I18n.format("loliasm.warnscreen.paragraph3.line4"), x, y += 9, textColor);
        drawString(fontRenderer, I18n.format("loliasm.warnscreen.paragraph3.line5"), x, y + 9, textColor);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

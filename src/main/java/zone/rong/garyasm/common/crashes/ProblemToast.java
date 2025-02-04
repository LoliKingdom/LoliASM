package zone.rong.garyasm.common.crashes;

import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.fml.common.ModContainer;

import java.util.Set;

public class ProblemToast implements IToast {

    private static final int ERROR_REPORT_DURATION = 30000;

    public final CrashReport report;

    public boolean hide;

    private String suspectedModString;

    public ProblemToast(CrashReport report) {
        this.report = report;
    }

    @Override
    public IToast.Visibility draw(GuiToast toastGui, long delta) {
        if (hide) {
            return Visibility.HIDE;
        }
        toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        toastGui.drawTexturedModalRect(0, 0, 0, 96, 160, 32);
        Object cause = getModCause();
        toastGui.getMinecraft().fontRenderer.drawString(cause.equals("") ?
                I18n.format("garyasm.notification.title.unknown") :
                I18n.format("garyasm.notification.title.mod", cause), 5, 7, 0xFF000000);
        toastGui.getMinecraft().fontRenderer.drawString(I18n.format("garyasm.notification.description"), 5, 18, 0xFF500050);
        return delta >= ERROR_REPORT_DURATION ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
    }

    private Object getModCause() {
        if (suspectedModString == null) {
            Set<ModContainer> suspectedMods = ((ICrashReportSuspectGetter) report).getSuspectedMods();
            suspectedModString = suspectedMods.isEmpty() ? "" : suspectedMods.iterator().next().getName();
        }
        return suspectedModString;
    }
}

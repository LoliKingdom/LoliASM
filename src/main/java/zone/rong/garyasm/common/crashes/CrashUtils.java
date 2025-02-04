package zone.rong.garyasm.common.crashes;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import zone.rong.garyasm.GaryLogger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class CrashUtils {

    public static final ThreadLocal<Boolean> WRITING_DETAIL = ThreadLocal.withInitial(() -> false);

    public static void crash(CrashReport report) {
        throw new ReportedException(report);
    }

    public static void warn(CrashReport report) {
        if (FMLLaunchHandler.side().isClient()) {
            outputReport(report);
            // Don't inline showWarningScreen, that will cause Java to load the GuiScreen
            // class on servers, because of the lambda!
            ((IMinecraftExtender) Minecraft.getMinecraft()).showWarningScreen(report);
            } else {
            GaryLogger.instance.fatal(report.getDescription(), report.getCrashCause());
        }
    }

    public static void notify(CrashReport report) {
        if (FMLLaunchHandler.side().isClient()) {
            outputReport(report);
            ((IMinecraftExtender) Minecraft.getMinecraft()).makeErrorNotification(report);
        } else {
            GaryLogger.instance.fatal(report.getDescription(), report.getCrashCause());
        }
    }

    public static void outputReport(CrashReport report) {
        try {
            if (report.getFile() == null) {
                String reportName = "crash-";
                reportName += new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
                reportName += Minecraft.getMinecraft().isCallingFromMinecraftThread() ? "-client" : "-server";
                reportName += ".txt";

                File reportsDir = FMLLaunchHandler.side().isClient() ? new File(Minecraft.getMinecraft().gameDir, "crash-reports") : new File("crash-reports");
                File reportFile = new File(reportsDir, reportName);

                report.saveToFile(reportFile);
            }
        } catch (Throwable e) {
            GaryLogger.instance.fatal("Failed saving report", e);
        }
        GaryLogger.instance.fatal("Minecraft ran into a problem! " + (report.getFile() != null ? "Report saved to: " + report.getFile() : "Crash report could not be saved.") + "\n" + report.getCompleteReport());
    }

}

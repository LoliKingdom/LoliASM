package zone.rong.blahajasm.common.crashes.mixins;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.spongepowered.asm.mixin.*;
import zone.rong.blahajasm.common.crashes.ModIdentifier;
import zone.rong.blahajasm.common.crashes.ICrashReportSuspectGetter;
import zone.rong.blahajasm.api.StacktraceDeobfuscator;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Mixin(value = CrashReport.class, priority = 500)
public abstract class CrashReportMixin implements ICrashReportSuspectGetter {

    @Shadow @Final private CrashReportCategory systemDetailsCategory;
    @Shadow @Final private Throwable cause;
    @Shadow @Final private List<CrashReportCategory> crashReportSections;
    @Shadow @Final private String description;

    @Shadow private static String getWittyComment() {
        throw new AssertionError();
    }

    @Shadow public abstract String getCauseStackTraceOrString();

    @Unique private Set<ModContainer> suspectedMods;

    @Override
    public Set<ModContainer> getSuspectedMods() {
        return suspectedMods;
    }

    /** @reason Adds a list of mods which may have caused the crash to the report. */
    @Inject(method = "populateEnvironment", at = @At("TAIL"))
    private void afterPopulateEnvironment(CallbackInfo ci) {
        systemDetailsCategory.addDetail("Suspected Mods", () -> {
            try {
                suspectedMods = ModIdentifier.identifyFromStacktrace(cause);
                String modListString = "Unknown";
                List<String> modNames = new ArrayList<>();
                for (ModContainer mod : suspectedMods) {
                    modNames.add(mod.getName() + " (" + mod.getModId() + ")");
                }
                if (!modNames.isEmpty()) {
                    modListString = StringUtils.join(modNames, ", ");
                }
                return modListString;
            } catch (Throwable e) {
                return ExceptionUtils.getStackTrace(e).replace("\t", "    ");
            }
        });
    }

    @Inject(method = "populateEnvironment", at = @At("HEAD"))
    private void beforePopulateEnvironment(CallbackInfo ci) {
        StacktraceDeobfuscator.deobfuscateThrowable(cause);
    }

    /**
     * @author VanillaFix
     * @reason Improve formatting
     */
    @Overwrite
    public String getCompleteReport() {
        StringBuilder builder = new StringBuilder();
        builder.append("---- Minecraft Crash Report ----\n")
                .append("// Blahajs deobfuscated this stacktrace using MCP's stable-39 mappings.\n")
                .append("// ").append(getWittyComment());
        String blame = getFunnyBlame();
        if (!blame.isEmpty()) {
            builder.append("// ").append(blame);
        }
        builder.append("\n\n")
                .append("Time: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date())).append("\n")
                .append("Description: ").append(description)
                .append("\n\n")
                .append(this.getCauseStackTraceOrString())
                .append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");
        for (int i = 0; i < 87; i++) {
            builder.append("-");
        }
        builder.append("\n\n");
        getSectionsInStringBuilder(builder);
        return builder.toString().replace("\t", "    ");
    }

    /**
     * @author VanillFix
     * @reason Improve report formatting, add blame comment
     */
    @Overwrite
    public void getSectionsInStringBuilder(StringBuilder builder) {
        for (CrashReportCategory crashreportcategory : crashReportSections) {
            crashreportcategory.appendToStringBuilder(builder);
            builder.append("\n");
        }
        systemDetailsCategory.appendToStringBuilder(builder);
    }

    private String getFunnyBlame() {
        try {
            if (Math.random() < 0.01 && !suspectedMods.isEmpty()) {
                ModContainer mod = suspectedMods.iterator().next();
                String author = mod.getMetadata().authorList.get(0);
                return "The blahajs blame " + author + "!";
            }
        } catch (Throwable ignored) {}
        return "";
    }

}

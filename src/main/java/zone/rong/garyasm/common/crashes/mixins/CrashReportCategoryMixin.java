package zone.rong.loliasm.common.crashes.mixins;

import net.minecraft.crash.CrashReportCategory;
import zone.rong.loliasm.api.StacktraceDeobfuscator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(CrashReportCategory.class)
public class CrashReportCategoryMixin {

    @Shadow @Final private String name;
    @Shadow @Final private List<CrashReportCategory_EntryInvoker> children;

    @Inject(method = "getPrunedStackTrace", at = @At(value = "INVOKE", target = "Ljava/lang/System;arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V", remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
    private void beforeCopyingStackTrace(int size, CallbackInfoReturnable<Integer> cir, StackTraceElement[] stackTrace) {
        StacktraceDeobfuscator.deobfuscateStacktrace(stackTrace);
    }

    /**
     * @author VanillaFix
     * @reason Improve formatting
     */
    @Overwrite
    public void appendToStringBuilder(StringBuilder builder) {
        builder.append("-- ").append(name).append(" --\n");
        for (CrashReportCategory_EntryInvoker entry : children) {
            String sectionIndent = "  ";
            builder.append(sectionIndent).append(entry.invokeGetKey()).append(": ");
            StringBuilder indent = new StringBuilder(sectionIndent + "  ");
            for (char ignored : entry.invokeGetKey().toCharArray()) {
                indent.append(" ");
            }
            boolean first = true;
            for (String line : entry.invokeGetValue().trim().split("\n")) {
                if (!first) {
                    builder.append("\n").append(indent);
                }
                first = false;
                if (line.startsWith("\t")) {
                    line = line.substring(1);
                }
                builder.append(line.replace("\t", ""));
            }
            builder.append("\n");
        }
    }

}

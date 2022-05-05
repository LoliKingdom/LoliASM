package zone.rong.loliasm.vanillafix.crashes.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.crash.CrashReportCategory$Entry")
public interface CrashReportCategory_EntryInvoker {

    @Invoker
    String invokeGetKey();

    @Invoker
    String invokeGetValue();

}

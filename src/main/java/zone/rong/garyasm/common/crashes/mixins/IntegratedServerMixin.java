package zone.rong.loliasm.common.crashes.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.ReportedException;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.common.crashes.IMinecraftExtender;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {

    @Shadow @Final private Minecraft mc;

    /**
     * @reason Checks if an integrated server crash was scheduled for this tick by the client, if Alt + F3 + C was pressed.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void beforeTick(CallbackInfo ci) {
        if (((IMinecraftExtender) mc).shouldCrashIntegratedServerNextTick()) {
            throw new ReportedException(new CrashReport("Manually triggered server-side debug crash", new Throwable()));
        }
    }

}

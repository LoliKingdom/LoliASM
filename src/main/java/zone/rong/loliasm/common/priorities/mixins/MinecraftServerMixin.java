package zone.rong.loliasm.common.priorities.mixins;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Redirect(method = "startServerThread", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;start()V"))
    private void setPriorityAndStart(Thread serverThread) {
        serverThread.setPriority(Thread.MIN_PRIORITY + 2);
        serverThread.start();
    }
}

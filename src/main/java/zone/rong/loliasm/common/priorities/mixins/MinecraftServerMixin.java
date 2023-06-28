package zone.rong.loliasm.common.priorities.mixins;

import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Shadow @Final private static Logger LOGGER;

    @Redirect(method = "startServerThread", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;start()V"), require = 0)
    private void setPriorityAndStart(Thread serverThread) {
        serverThread.setPriority(Thread.MIN_PRIORITY + 2);
        serverThread.start();
        LOGGER.debug("LoliASM: Started server thread, with {} priority", serverThread.getPriority());
    }
}

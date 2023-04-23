package zone.rong.loliasm.common.priorities.mixins.client;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkRenderDispatcher.class)
public class ChunkRenderDispatcherMixin {
    @Shadow @Final @Mutable
    private int countRenderBuilders;

    @Shadow @Final private static Logger LOGGER;

    private int getRenderBuilderCount() {
        int processors = Runtime.getRuntime().availableProcessors();
        boolean allowSingleThread = Runtime.getRuntime().availableProcessors() < 2;
        return MathHelper.clamp(Math.max(processors / 3, processors - 6), allowSingleThread ? 1 : 2, 10);
    }

    @ModifyVariable(method = "<init>(I)V", at = @At("STORE"), index = 3, ordinal = 2)
    private int setBuilders(int original) {
        return getRenderBuilderCount();
    }

    @Redirect(method = "<init>(I)V", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;countRenderBuilders:I"))
    private void setBuilders(ChunkRenderDispatcher dispatcher, int original) {
        int nThreads = getRenderBuilderCount();
        /* we need more builder objects in the queue than number of threads, because threads don't free them immediately */
        this.countRenderBuilders = nThreads * 10;
        LOGGER.info("Creating {} chunk builders", nThreads);
    }

    @Redirect(method = "<init>(I)V", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;start()V"))
    private void setPriorityAndStart(Thread workerThread) {
        workerThread.setPriority(Thread.MIN_PRIORITY + 1);
        workerThread.start();
    }
}

package zone.rong.blahajasm.common.mcfixes.mixins.skindownloading;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Mixin(ThreadDownloadImageData.class)
public class ThreadDownloadImageDataMixin {

    @Unique private static final Executor EXECUTOR = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
            new ThreadFactoryBuilder()
                    .setNameFormat("Skin Downloader #%d")
                    .setDaemon(true)
                    .setPriority(Thread.MIN_PRIORITY)
                    .build());

    @Redirect(method = "loadTextureFromServer", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;start()V", remap = false))
    private void onThreadStart(Thread thread) {
        EXECUTOR.execute(thread);
    }

}

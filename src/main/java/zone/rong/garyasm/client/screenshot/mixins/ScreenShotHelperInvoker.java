package zone.rong.garyasm.client.screenshot.mixins;

import net.minecraft.util.ScreenShotHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.File;

@Mixin(ScreenShotHelper.class)
public interface ScreenShotHelperInvoker {

    @Invoker
    static File invokeGetTimestampedPNGFileForDirectory(File gameDirectory) {
        throw new AssertionError();
    }

}

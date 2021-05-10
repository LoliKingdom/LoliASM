package zone.rong.loliasm.common.misc.mixins;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = FluidRegistry.class, remap = false)
public class FluidRegistryMixin {

    @Shadow static boolean universalBucketEnabled;

    /**
     * @author Rongmario
     * @reason Disables the hasReachedState check if {@link FluidRegistryMixin#universalBucketEnabled} is already true
     */
    @Overwrite(remap = false)
    public static void enableUniversalBucket() {
        if (universalBucketEnabled) {
            return;
        }
        if (Loader.instance().hasReachedState(LoaderState.PREINITIALIZATION)) {
            ModContainer modContainer = Loader.instance().activeModContainer();
            String modContainerName = modContainer == null ? null : modContainer.getName();
            FMLLog.log.error("Trying to activate the universal filled bucket too late. Call it statically in your Mods class. Mod: {}", modContainerName);
        } else {
            universalBucketEnabled = true;
        }
    }

}

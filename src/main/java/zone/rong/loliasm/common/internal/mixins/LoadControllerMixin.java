package zone.rong.loliasm.common.internal.mixins;

import com.google.common.base.Strings;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.ThreadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.loliasm.spark.LoliSparker;

import javax.annotation.Nullable;

@Mixin(value = LoadController.class, priority = -1000, remap = false)
public abstract class LoadControllerMixin {

    @Shadow(remap = false) private ModContainer activeContainer;

    @Shadow(remap = false) @Nullable protected abstract ModContainer findActiveContainerFromStack();

    /**
     * @author Rongmario
     * @reason Allow a faster lookup through ThreadContext first as some contexts submit modIds through this way
     */
    @Nullable
    @Overwrite
    public ModContainer activeContainer() {
        if (activeContainer == null) {
            String modId = ThreadContext.get("mod");
            if (Strings.isNullOrEmpty(modId)) {
                return findActiveContainerFromStack();
            }
            ModContainer container = Loader.instance().getIndexedModList().get(modId);
            return container == null ? findActiveContainerFromStack() : container;
        }
        return activeContainer;
    }

    /**
     * MixinBooter injects into this exact same method
     */
    @Inject(method = "distributeStateMessage(Lnet/minecraftforge/fml/common/LoaderState;[Ljava/lang/Object;)V", at = @At("HEAD"))
    private void injectBeforeDistributingState(LoaderState state, Object[] eventData, CallbackInfo ci) {
        if (!Loader.isModLoaded("spark")) {
            return;
        }
        switch (state) {
            case CONSTRUCTING:
                if (LoliConfig.instance.sparkProfileCoreModLoading) {
                    LoliSparker.stop("coremod");
                }
                if (LoliConfig.instance.sparkProfileConstructionStage) {
                    LoliSparker.start(LoaderState.CONSTRUCTING.toString());
                }
                break;
            case PREINITIALIZATION:
                if (LoliConfig.instance.sparkProfilePreInitializationStage) {
                    LoliSparker.start(LoaderState.PREINITIALIZATION.toString());
                }
                break;
            case INITIALIZATION:
                if (LoliConfig.instance.sparkProfileInitializationStage) {
                    LoliSparker.start(LoaderState.INITIALIZATION.toString());
                }
                break;
            case POSTINITIALIZATION:
                if (LoliConfig.instance.sparkProfilePostInitializationStage) {
                    LoliSparker.start(LoaderState.POSTINITIALIZATION.toString());
                }
                break;
            case AVAILABLE:
                if (LoliConfig.instance.sparkProfileLoadCompleteStage) {
                    LoliSparker.start(LoaderState.AVAILABLE.toString());
                }
                break;
        }
    }

    @Inject(method = "distributeStateMessage(Lnet/minecraftforge/fml/common/LoaderState;[Ljava/lang/Object;)V", at = @At("RETURN"))
    private void injectAfterDistributingState(LoaderState state, Object[] eventData, CallbackInfo ci) {
        if (!Loader.isModLoaded("spark")) {
            return;
        }
        switch (state) {
            case CONSTRUCTING:
                if (LoliConfig.instance.sparkProfileConstructionStage) {
                    LoliSparker.stop(LoaderState.CONSTRUCTING.toString());
                }
                break;
            case PREINITIALIZATION:
                if (LoliConfig.instance.sparkProfilePreInitializationStage) {
                    LoliSparker.stop(LoaderState.PREINITIALIZATION.toString());
                }
                break;
            case INITIALIZATION:
                if (LoliConfig.instance.sparkProfileInitializationStage) {
                    LoliSparker.stop(LoaderState.INITIALIZATION.toString());
                }
                break;
            case POSTINITIALIZATION:
                if (LoliConfig.instance.sparkProfilePostInitializationStage) {
                    LoliSparker.stop(LoaderState.POSTINITIALIZATION.toString());
                }
                break;
            case AVAILABLE:
                if (LoliConfig.instance.sparkProfileLoadCompleteStage) {
                    LoliSparker.stop(LoaderState.AVAILABLE.toString());
                }
                if (LoliConfig.instance.sparkProfileEntireGameLoad) {
                    LoliSparker.stop("game");
                }
                break;
        }
    }

}

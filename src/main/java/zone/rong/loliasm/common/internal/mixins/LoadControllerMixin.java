package zone.rong.loliasm.common.internal.mixins;

import com.google.common.base.Strings;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.ThreadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.loliasm.spark.LoliSparker;

import javax.annotation.Nullable;

@Mixin(value = LoadController.class, priority = -1000, remap = false)
public abstract class LoadControllerMixin {

    @Shadow(remap = false) private ModContainer activeContainer;

    @Unique private static Boolean hasSpark;
    @Unique private static boolean gameHasLoaded = false;

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
    @Inject(method = "propogateStateMessage", at = @At("HEAD"))
    private void injectBeforeDistributingState(FMLEvent stateEvent, CallbackInfo ci) {
        if (hasSpark == null) {
            hasSpark = Loader.isModLoaded("spark");
        }
        if (hasSpark) {
            if (stateEvent instanceof FMLStateEvent) {
                if (stateEvent instanceof FMLConstructionEvent) {
                    if (LoliConfig.instance.sparkProfileCoreModLoading) {
                        LoliSparker.stop("coremod");
                    }
                    if (LoliConfig.instance.sparkProfileConstructionStage) {
                        LoliSparker.start(LoaderState.CONSTRUCTING.toString());
                    }
                } else if (stateEvent instanceof FMLPreInitializationEvent) {
                    if (LoliConfig.instance.sparkProfileConstructionStage) {
                        LoliSparker.stop(LoaderState.CONSTRUCTING.toString());
                    }
                    if (LoliConfig.instance.sparkProfilePreInitializationStage) {
                        LoliSparker.start(LoaderState.PREINITIALIZATION.toString());
                    }
                } else if (stateEvent instanceof FMLInitializationEvent) {
                    if (LoliConfig.instance.sparkProfilePreInitializationStage) {
                        LoliSparker.stop(LoaderState.PREINITIALIZATION.toString());
                    }
                    if (LoliConfig.instance.sparkProfileInitializationStage) {
                        LoliSparker.start(LoaderState.INITIALIZATION.toString());
                    }
                } else if (stateEvent instanceof FMLPostInitializationEvent) {
                    if (LoliConfig.instance.sparkProfileInitializationStage) {
                        LoliSparker.stop(LoaderState.INITIALIZATION.toString());
                    }
                    if (LoliConfig.instance.sparkProfilePostInitializationStage) {
                        LoliSparker.start(LoaderState.POSTINITIALIZATION.toString());
                    }
                } else if (stateEvent instanceof FMLLoadCompleteEvent) {
                    if (LoliConfig.instance.sparkProfilePostInitializationStage) {
                        LoliSparker.stop(LoaderState.POSTINITIALIZATION.toString());
                    }
                    if (LoliConfig.instance.sparkProfileLoadCompleteStage) {
                        LoliSparker.start(LoaderState.AVAILABLE.toString());
                    }
                } else if (stateEvent instanceof FMLServerAboutToStartEvent) {
                    if (LoliConfig.instance.sparkProfileWorldAboutToStartStage) {
                        LoliSparker.start(LoaderState.SERVER_ABOUT_TO_START.toString());
                    }
                    if (LoliConfig.instance.sparkProfileEntireWorldLoad) {
                        LoliSparker.start("world");
                    }
                } else if (stateEvent instanceof FMLServerStartingEvent) {
                    if (LoliConfig.instance.sparkProfileWorldAboutToStartStage) {
                        LoliSparker.stop(LoaderState.SERVER_ABOUT_TO_START.toString());
                    }
                    if (LoliConfig.instance.sparkProfileWorldStartingStage) {
                        LoliSparker.start(LoaderState.SERVER_STARTING.toString());
                    }
                } else if (stateEvent instanceof FMLServerStartedEvent) {
                    if (LoliConfig.instance.sparkProfileWorldStartingStage) {
                        LoliSparker.stop(LoaderState.SERVER_STARTING.toString());
                    }
                    if (LoliConfig.instance.sparkProfileWorldStartedStage) {
                        LoliSparker.start(LoaderState.SERVER_STARTED.toString());
                    }
                }
            } else if (stateEvent instanceof FMLModIdMappingEvent && !gameHasLoaded && ((FMLModIdMappingEvent) stateEvent).isFrozen) {
                if (LoliConfig.instance.sparkProfileFinalizingStage) {
                    LoliSparker.start("finalizing");
                }
            }
        }
    }

    @Inject(method = "propogateStateMessage", at = @At("RETURN"))
    private void injectAfterDistributingState(FMLEvent stateEvent, CallbackInfo ci) {
        if (hasSpark) {
            if (stateEvent instanceof FMLStateEvent) {
                if (stateEvent instanceof FMLLoadCompleteEvent) {
                    if (LoliConfig.instance.sparkProfileLoadCompleteStage) {
                        LoliSparker.stop(LoaderState.AVAILABLE.toString());
                    }
                } else if (stateEvent instanceof FMLServerStartedEvent) {
                    if (LoliConfig.instance.sparkProfileWorldStartedStage) {
                        LoliSparker.stop(LoaderState.SERVER_STARTED.toString());
                    }
                    if (LoliConfig.instance.sparkProfileEntireWorldLoad) {
                        LoliSparker.stop("world");
                    }
                    if (LoliConfig.instance.sparkSummarizeHeapSpaceAfterWorldLoads) {
                        LoliSparker.checkHeap(true, true);
                    }
                }
            } else if (stateEvent instanceof FMLModIdMappingEvent && !gameHasLoaded && ((FMLModIdMappingEvent) stateEvent).isFrozen) {
                if (LoliConfig.instance.sparkProfileFinalizingStage) {
                    LoliSparker.stop("finalizing");
                    gameHasLoaded = true; // Don't profile when this fires on serverStopped etc
                }
                if (LoliConfig.instance.sparkProfileEntireGameLoad) {
                    LoliSparker.stop("game");
                }
                if (LoliConfig.instance.sparkSummarizeHeapSpaceAfterGameLoads) {
                    LoliSparker.checkHeap(true, true);
                }
            }
        }
    }

}

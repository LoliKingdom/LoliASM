package zone.rong.blahajasm.common.internal.mixins;

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
import zone.rong.blahajasm.config.BlahajConfig;
import zone.rong.blahajasm.spark.BlahajSparker;

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
                    if (BlahajConfig.instance.sparkProfileCoreModLoading) {
                        BlahajSparker.stop("coremod");
                    }
                    if (BlahajConfig.instance.sparkProfileConstructionStage) {
                        BlahajSparker.start(LoaderState.CONSTRUCTING.toString());
                    }
                } else if (stateEvent instanceof FMLPreInitializationEvent) {
                    if (BlahajConfig.instance.sparkProfileConstructionStage) {
                        BlahajSparker.stop(LoaderState.CONSTRUCTING.toString());
                    }
                    if (BlahajConfig.instance.sparkProfilePreInitializationStage) {
                        BlahajSparker.start(LoaderState.PREINITIALIZATION.toString());
                    }
                } else if (stateEvent instanceof FMLInitializationEvent) {
                    if (BlahajConfig.instance.sparkProfilePreInitializationStage) {
                        BlahajSparker.stop(LoaderState.PREINITIALIZATION.toString());
                    }
                    if (BlahajConfig.instance.sparkProfileInitializationStage) {
                        BlahajSparker.start(LoaderState.INITIALIZATION.toString());
                    }
                } else if (stateEvent instanceof FMLPostInitializationEvent) {
                    if (BlahajConfig.instance.sparkProfileInitializationStage) {
                        BlahajSparker.stop(LoaderState.INITIALIZATION.toString());
                    }
                    if (BlahajConfig.instance.sparkProfilePostInitializationStage) {
                        BlahajSparker.start(LoaderState.POSTINITIALIZATION.toString());
                    }
                } else if (stateEvent instanceof FMLLoadCompleteEvent) {
                    if (BlahajConfig.instance.sparkProfilePostInitializationStage) {
                        BlahajSparker.stop(LoaderState.POSTINITIALIZATION.toString());
                    }
                    if (BlahajConfig.instance.sparkProfileLoadCompleteStage) {
                        BlahajSparker.start(LoaderState.AVAILABLE.toString());
                    }
                } else if (stateEvent instanceof FMLServerAboutToStartEvent) {
                    if (BlahajConfig.instance.sparkProfileWorldAboutToStartStage) {
                        BlahajSparker.start(LoaderState.SERVER_ABOUT_TO_START.toString());
                    }
                    if (BlahajConfig.instance.sparkProfileEntireWorldLoad) {
                        BlahajSparker.start("world");
                    }
                } else if (stateEvent instanceof FMLServerStartingEvent) {
                    if (BlahajConfig.instance.sparkProfileWorldAboutToStartStage) {
                        BlahajSparker.stop(LoaderState.SERVER_ABOUT_TO_START.toString());
                    }
                    if (BlahajConfig.instance.sparkProfileWorldStartingStage) {
                        BlahajSparker.start(LoaderState.SERVER_STARTING.toString());
                    }
                } else if (stateEvent instanceof FMLServerStartedEvent) {
                    if (BlahajConfig.instance.sparkProfileWorldStartingStage) {
                        BlahajSparker.stop(LoaderState.SERVER_STARTING.toString());
                    }
                    if (BlahajConfig.instance.sparkProfileWorldStartedStage) {
                        BlahajSparker.start(LoaderState.SERVER_STARTED.toString());
                    }
                }
            } else if (stateEvent instanceof FMLModIdMappingEvent && !gameHasLoaded && ((FMLModIdMappingEvent) stateEvent).isFrozen) {
                if (BlahajConfig.instance.sparkProfileFinalizingStage) {
                    BlahajSparker.start("finalizing");
                }
            }
        }
    }

    @Inject(method = "propogateStateMessage", at = @At("RETURN"))
    private void injectAfterDistributingState(FMLEvent stateEvent, CallbackInfo ci) {
        if (hasSpark) {
            if (stateEvent instanceof FMLStateEvent) {
                if (stateEvent instanceof FMLLoadCompleteEvent) {
                    if (BlahajConfig.instance.sparkProfileLoadCompleteStage) {
                        BlahajSparker.stop(LoaderState.AVAILABLE.toString());
                    }
                } else if (stateEvent instanceof FMLServerStartedEvent) {
                    if (BlahajConfig.instance.sparkProfileWorldStartedStage) {
                        BlahajSparker.stop(LoaderState.SERVER_STARTED.toString());
                    }
                    if (BlahajConfig.instance.sparkProfileEntireWorldLoad) {
                        BlahajSparker.stop("world");
                    }
                    if (BlahajConfig.instance.sparkSummarizeHeapSpaceAfterWorldLoads) {
                        BlahajSparker.checkHeap(true, true);
                    }
                }
            } else if (stateEvent instanceof FMLModIdMappingEvent && !gameHasLoaded && ((FMLModIdMappingEvent) stateEvent).isFrozen) {
                if (BlahajConfig.instance.sparkProfileFinalizingStage) {
                    BlahajSparker.stop("finalizing");
                    gameHasLoaded = true; // Don't profile when this fires on serverStopped etc
                }
                if (BlahajConfig.instance.sparkProfileEntireGameLoad) {
                    BlahajSparker.stop("game");
                }
                if (BlahajConfig.instance.sparkSummarizeHeapSpaceAfterGameLoads) {
                    BlahajSparker.checkHeap(true, true);
                }
            }
        }
    }

}

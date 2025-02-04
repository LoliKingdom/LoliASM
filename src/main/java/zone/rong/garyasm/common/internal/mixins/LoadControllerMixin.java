package zone.rong.garyasm.common.internal.mixins;

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
import zone.rong.garyasm.config.GaryConfig;
import zone.rong.garyasm.spark.GarySparker;

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
                    if (GaryConfig.instance.sparkProfileCoreModLoading) {
                        GarySparker.stop("coremod");
                    }
                    if (GaryConfig.instance.sparkProfileConstructionStage) {
                        GarySparker.start(LoaderState.CONSTRUCTING.toString());
                    }
                } else if (stateEvent instanceof FMLPreInitializationEvent) {
                    if (GaryConfig.instance.sparkProfileConstructionStage) {
                        GarySparker.stop(LoaderState.CONSTRUCTING.toString());
                    }
                    if (GaryConfig.instance.sparkProfilePreInitializationStage) {
                        GarySparker.start(LoaderState.PREINITIALIZATION.toString());
                    }
                } else if (stateEvent instanceof FMLInitializationEvent) {
                    if (GaryConfig.instance.sparkProfilePreInitializationStage) {
                        GarySparker.stop(LoaderState.PREINITIALIZATION.toString());
                    }
                    if (GaryConfig.instance.sparkProfileInitializationStage) {
                        GarySparker.start(LoaderState.INITIALIZATION.toString());
                    }
                } else if (stateEvent instanceof FMLPostInitializationEvent) {
                    if (GaryConfig.instance.sparkProfileInitializationStage) {
                        GarySparker.stop(LoaderState.INITIALIZATION.toString());
                    }
                    if (GaryConfig.instance.sparkProfilePostInitializationStage) {
                        GarySparker.start(LoaderState.POSTINITIALIZATION.toString());
                    }
                } else if (stateEvent instanceof FMLLoadCompleteEvent) {
                    if (GaryConfig.instance.sparkProfilePostInitializationStage) {
                        GarySparker.stop(LoaderState.POSTINITIALIZATION.toString());
                    }
                    if (GaryConfig.instance.sparkProfileLoadCompleteStage) {
                        GarySparker.start(LoaderState.AVAILABLE.toString());
                    }
                } else if (stateEvent instanceof FMLServerAboutToStartEvent) {
                    if (GaryConfig.instance.sparkProfileWorldAboutToStartStage) {
                        GarySparker.start(LoaderState.SERVER_ABOUT_TO_START.toString());
                    }
                    if (GaryConfig.instance.sparkProfileEntireWorldLoad) {
                        GarySparker.start("world");
                    }
                } else if (stateEvent instanceof FMLServerStartingEvent) {
                    if (GaryConfig.instance.sparkProfileWorldAboutToStartStage) {
                        GarySparker.stop(LoaderState.SERVER_ABOUT_TO_START.toString());
                    }
                    if (GaryConfig.instance.sparkProfileWorldStartingStage) {
                        GarySparker.start(LoaderState.SERVER_STARTING.toString());
                    }
                } else if (stateEvent instanceof FMLServerStartedEvent) {
                    if (GaryConfig.instance.sparkProfileWorldStartingStage) {
                        GarySparker.stop(LoaderState.SERVER_STARTING.toString());
                    }
                    if (GaryConfig.instance.sparkProfileWorldStartedStage) {
                        GarySparker.start(LoaderState.SERVER_STARTED.toString());
                    }
                }
            } else if (stateEvent instanceof FMLModIdMappingEvent && !gameHasLoaded && ((FMLModIdMappingEvent) stateEvent).isFrozen) {
                if (GaryConfig.instance.sparkProfileFinalizingStage) {
                    GarySparker.start("finalizing");
                }
            }
        }
    }

    @Inject(method = "propogateStateMessage", at = @At("RETURN"))
    private void injectAfterDistributingState(FMLEvent stateEvent, CallbackInfo ci) {
        if (hasSpark) {
            if (stateEvent instanceof FMLStateEvent) {
                if (stateEvent instanceof FMLLoadCompleteEvent) {
                    if (GaryConfig.instance.sparkProfileLoadCompleteStage) {
                        GarySparker.stop(LoaderState.AVAILABLE.toString());
                    }
                } else if (stateEvent instanceof FMLServerStartedEvent) {
                    if (GaryConfig.instance.sparkProfileWorldStartedStage) {
                        GarySparker.stop(LoaderState.SERVER_STARTED.toString());
                    }
                    if (GaryConfig.instance.sparkProfileEntireWorldLoad) {
                        GarySparker.stop("world");
                    }
                    if (GaryConfig.instance.sparkSummarizeHeapSpaceAfterWorldLoads) {
                        GarySparker.checkHeap(true, true);
                    }
                }
            } else if (stateEvent instanceof FMLModIdMappingEvent && !gameHasLoaded && ((FMLModIdMappingEvent) stateEvent).isFrozen) {
                if (GaryConfig.instance.sparkProfileFinalizingStage) {
                    GarySparker.stop("finalizing");
                    gameHasLoaded = true; // Don't profile when this fires on serverStopped etc
                }
                if (GaryConfig.instance.sparkProfileEntireGameLoad) {
                    GarySparker.stop("game");
                }
                if (GaryConfig.instance.sparkSummarizeHeapSpaceAfterGameLoads) {
                    GarySparker.checkHeap(true, true);
                }
            }
        }
    }

}

package zone.rong.loliasm.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.bakedquad.LoliVertexDataPool;
import zone.rong.loliasm.client.models.bucket.LoliBakedDynBucket;
import zone.rong.loliasm.client.screenshot.ScreenshotListener;
import zone.rong.loliasm.client.sprite.FramesTextureData;
import zone.rong.loliasm.common.modfixes.qmd.QMDEventHandler;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.loliasm.core.LoliTransformer;

import java.util.ArrayList;
import java.util.List;


@Mod.EventBusSubscriber(modid = "loliasm", value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static final List<Runnable> refreshAfterModels = new ArrayList<>();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        if (LoliConfig.instance.releaseSpriteFramesCache) {
            MinecraftForge.EVENT_BUS.register(FramesTextureData.class);
        }
        if (Loader.isModLoaded("qmd") && LoliConfig.instance.optimizeQMDBeamRenderer) {
            MinecraftForge.EVENT_BUS.register(QMDEventHandler.class);
        }
        if (LoliConfig.instance.copyScreenshotToClipboard) {
            MinecraftForge.EVENT_BUS.register(ScreenshotListener.class);
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        if (!Loader.isModLoaded("jei")) {
            releaseSpriteFramesCache();
        }
    }

    @Override
    public void loadComplete(FMLLoadCompleteEvent event) {
        super.loadComplete(event);
        if (Loader.isModLoaded("jei")) {
            releaseSpriteFramesCache();
        }
        if (!LoliTransformer.isOptifineInstalled && LoliConfig.instance.vertexDataCanonicalization) {
            LoliLogger.instance.info("{} total quads processed. {} unique vertex data array in LoliVertexDataPool, {} vertex data arrays deduplicated altogether during game load.", LoliVertexDataPool.getDeduplicatedCount(), LoliVertexDataPool.getSize(), LoliVertexDataPool.getDeduplicatedCount() - LoliVertexDataPool.getSize());
            MinecraftForge.EVENT_BUS.register(LoliVertexDataPool.class);
        }
    }

    private void releaseSpriteFramesCache() {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener((ISelectiveResourceReloadListener) (manager, predicate) -> {
            if (predicate.test(VanillaResourceType.MODELS)) {
                refreshAfterModels.forEach(Runnable::run);
                if (LoliConfig.instance.reuseBucketQuads) {
                    LoliBakedDynBucket.baseQuads.clear();
                    LoliBakedDynBucket.flippedBaseQuads.clear();
                    LoliBakedDynBucket.coverQuads.clear();
                    LoliBakedDynBucket.flippedCoverQuads.clear();
                }
                if (!LoliTransformer.isOptifineInstalled && LoliConfig.instance.vertexDataCanonicalization) {
                    LoliVertexDataPool.invalidate();
                }
            }
        });
    }
}

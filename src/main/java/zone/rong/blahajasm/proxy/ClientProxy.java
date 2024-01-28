package zone.rong.blahajasm.proxy;

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
import zone.rong.blahajasm.BlahajLogger;
import zone.rong.blahajasm.BlahajReflector;
import zone.rong.blahajasm.bakedquad.BlahajVertexDataPool;
import zone.rong.blahajasm.client.models.bucket.BlahajBakedDynBucket;
import zone.rong.blahajasm.client.screenshot.ScreenshotListener;
import zone.rong.blahajasm.client.sprite.FramesTextureData;
import zone.rong.blahajasm.common.modfixes.qmd.QMDEventHandler;
import zone.rong.blahajasm.config.BlahajConfig;
import zone.rong.blahajasm.core.BlahajTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "blahajasm", value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static final List<Runnable> refreshAfterModels = new ArrayList<>();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        if (BlahajConfig.instance.releaseSpriteFramesCache) {
            MinecraftForge.EVENT_BUS.register(FramesTextureData.class);
        }
        if (Loader.isModLoaded("qmd") && BlahajConfig.instance.optimizeQMDBeamRenderer) {
            MinecraftForge.EVENT_BUS.register(QMDEventHandler.class);
        }
        if (BlahajConfig.instance.copyScreenshotToClipboard) {
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
        if (!BlahajTransformer.isOptifineInstalled && BlahajConfig.instance.vertexDataCanonicalization) {
            BlahajLogger.instance.info("{} total quads processed. {} unique vertex data array in BlahajVertexDataPool, {} vertex data arrays deduplicated altogether during game load.", BlahajVertexDataPool.getDeduplicatedCount(), BlahajVertexDataPool.getSize(), BlahajVertexDataPool.getDeduplicatedCount() - BlahajVertexDataPool.getSize());
            MinecraftForge.EVENT_BUS.register(BlahajVertexDataPool.class);
        }
    }

    private void releaseSpriteFramesCache() {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener((ISelectiveResourceReloadListener) (manager, predicate) -> {
            if (predicate.test(VanillaResourceType.MODELS)) {
                refreshAfterModels.forEach(Runnable::run);
                if (BlahajConfig.instance.reuseBucketQuads) {
                    BlahajBakedDynBucket.baseQuads.clear();
                    BlahajBakedDynBucket.flippedBaseQuads.clear();
                    BlahajBakedDynBucket.coverQuads.clear();
                    BlahajBakedDynBucket.flippedCoverQuads.clear();
                }
                if (!BlahajTransformer.isOptifineInstalled && BlahajConfig.instance.vertexDataCanonicalization) {
                    BlahajVertexDataPool.invalidate();
                }
            }
        });
    }
}

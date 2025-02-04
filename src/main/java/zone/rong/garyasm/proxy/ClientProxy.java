package zone.rong.garyasm.proxy;

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
import zone.rong.garyasm.GaryLogger;
import zone.rong.garyasm.bakedquad.GaryVertexDataPool;
import zone.rong.garyasm.client.models.bucket.GaryBakedDynBucket;
import zone.rong.garyasm.client.screenshot.ScreenshotListener;
import zone.rong.garyasm.client.sprite.FramesTextureData;
import zone.rong.garyasm.common.modfixes.qmd.QMDEventHandler;
import zone.rong.garyasm.config.GaryConfig;
import zone.rong.garyasm.core.GaryTransformer;

import java.util.ArrayList;
import java.util.List;


@Mod.EventBusSubscriber(modid = "garyasm", value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static final List<Runnable> refreshAfterModels = new ArrayList<>();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        if (GaryConfig.instance.releaseSpriteFramesCache) {
            MinecraftForge.EVENT_BUS.register(FramesTextureData.class);
        }
        if (Loader.isModLoaded("qmd") && GaryConfig.instance.optimizeQMDBeamRenderer) {
            MinecraftForge.EVENT_BUS.register(QMDEventHandler.class);
        }
        if (GaryConfig.instance.copyScreenshotToClipboard) {
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
        if (!GaryTransformer.isOptifineInstalled && GaryConfig.instance.vertexDataCanonicalization) {
            GaryLogger.instance.info("{} total quads processed. {} unique vertex data array in GaryVertexDataPool, {} vertex data arrays deduplicated altogether during game load.", GaryVertexDataPool.getDeduplicatedCount(), GaryVertexDataPool.getSize(), GaryVertexDataPool.getDeduplicatedCount() - GaryVertexDataPool.getSize());
            MinecraftForge.EVENT_BUS.register(GaryVertexDataPool.class);
        }
    }

    private void releaseSpriteFramesCache() {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener((ISelectiveResourceReloadListener) (manager, predicate) -> {
            if (predicate.test(VanillaResourceType.MODELS)) {
                refreshAfterModels.forEach(Runnable::run);
                if (GaryConfig.instance.reuseBucketQuads) {
                    GaryBakedDynBucket.baseQuads.clear();
                    GaryBakedDynBucket.flippedBaseQuads.clear();
                    GaryBakedDynBucket.coverQuads.clear();
                    GaryBakedDynBucket.flippedCoverQuads.clear();
                }
                if (!GaryTransformer.isOptifineInstalled && GaryConfig.instance.vertexDataCanonicalization) {
                    GaryVertexDataPool.invalidate();
                }
            }
        });
    }
}

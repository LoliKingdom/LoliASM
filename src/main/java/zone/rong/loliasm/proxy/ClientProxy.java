package zone.rong.loliasm.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.LoliReflector;
import zone.rong.loliasm.client.sprite.FramesTextureData;
import zone.rong.loliasm.config.LoliConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "loliasm", value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public static final List<Runnable> refreshAfterModels = new ArrayList<>();

    public static boolean canReload = false;

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END && FramesTextureData.scheduledToReleaseCache != null) {
            for (TextureAtlasSprite sprite : FramesTextureData.scheduledToReleaseCache) {
                LoliLogger.instance.error("Releasing cache of {}", sprite.getIconName());
                sprite.clearFramesTextureData();
            }
            FramesTextureData.scheduledToReleaseCache = null;
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        if (!Loader.isModLoaded("jei")) {
            releaseSpriteFramesCache();
        }
    }

    @Override
    public void loadComplete(FMLLoadCompleteEvent event) {
        if (Loader.isModLoaded("jei")) {
            releaseSpriteFramesCache();
        }
    }

    private void releaseSpriteFramesCache() {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener((ISelectiveResourceReloadListener) (manager, predicate) -> {
            if (predicate.test(VanillaResourceType.MODELS)) {
                refreshAfterModels.forEach(Runnable::run);
                if (LoliConfig.instance.releaseSpriteFramesCache) {
                    canReload = false;
                    int count = 0;
                    try {
                        for (TextureAtlasSprite sprite : ((Map<String, TextureAtlasSprite>) LoliReflector.resolveFieldGetter(TextureMap.class, "mapRegisteredSprites", "field_110574_e").invoke(Minecraft.getMinecraft().getTextureMapBlocks())).values()) {
                            if (!sprite.hasAnimationMetadata() /* && sprite.getClass() == TextureAtlasSprite.class*/) {
                                count++;
                                sprite.setFramesTextureData(new FramesTextureData(sprite));
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    canReload = true;
                    LoliLogger.instance.info("{} TextureAtlasSprites had their framesTextureData released", count);
                }
            }
        });
    }
}

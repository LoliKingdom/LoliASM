package zone.rong.loliasm.client.sprite;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.proxy.ClientProxy;

import java.io.IOException;
import java.util.*;

public class FramesTextureData extends ArrayList<int[][]> {

    private static final Set<TextureAtlasSprite> scheduledToReleaseCache = Collections.newSetFromMap(new WeakHashMap<>());

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !FramesTextureData.scheduledToReleaseCache.isEmpty()) {
            for (Iterator<TextureAtlasSprite> iter = scheduledToReleaseCache.iterator(); iter.hasNext();) {
                TextureAtlasSprite sprite = iter.next();
                if (sprite != null) {
                    try {
                        sprite.clearFramesTextureData();
                    } catch (NullPointerException e) {
                        LoliLogger.instance.error("NullPointerException: Trying to clear {}'s FramesTextureData but unable to!", sprite.getIconName());
                    }
                }
                iter.remove();
            }
        }
    }

    private final TextureAtlasSprite sprite;

    public FramesTextureData(TextureAtlasSprite sprite) {
        super();
        this.sprite = sprite;
    }

    @Override
    public int[][] get(int index) {
        if (ClientProxy.canReload && super.isEmpty()) {
            load();
            Minecraft.getMinecraft().addScheduledTask(() -> scheduledToReleaseCache.add(sprite));
        }
        return super.get(index);
    }

    @Override
    public int size() {
        if (ClientProxy.canReload && super.isEmpty()) {
            load();
            Minecraft.getMinecraft().addScheduledTask(() -> scheduledToReleaseCache.add(sprite));
        }
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        if (ClientProxy.canReload && super.isEmpty()) {
            load();
            Minecraft.getMinecraft().addScheduledTask(() -> scheduledToReleaseCache.add(sprite));
        }
        return super.isEmpty();
    }

    @Override
    public void clear() {
        super.clear();
        trimToSize();
    }

    private void load() {
        ResourceLocation location = getLocation();
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
        if (sprite.hasCustomLoader(resourceManager, location)) {
            sprite.load(resourceManager, location, rl -> textureMap.getAtlasSprite(rl.toString()));
        } else {
            try (IResource resource = resourceManager.getResource(location)) {
                sprite.loadSpriteFrames(resource, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ResourceLocation getLocation() {
        String[] parts = ResourceLocation.splitObjectName(sprite.getIconName());
        return new ResourceLocation(parts[0], String.format("%s/%s%s", Minecraft.getMinecraft().getTextureMapBlocks().getBasePath(), parts[1], ".png"));
    }
}

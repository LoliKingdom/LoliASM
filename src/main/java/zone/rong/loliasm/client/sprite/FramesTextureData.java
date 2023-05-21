package zone.rong.loliasm.client.sprite;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.common.internal.mixins.TextureAtlasSpriteAccessor;
import zone.rong.loliasm.common.internal.mixins.TextureMapAccessor;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FramesTextureData extends ArrayList<int[][]> {
    private static boolean canReload = true;

    private static final Class<?> FOAMFIX_SPRITE;

    static {
        Class<?> ffSprite;
        try {
            ffSprite = Class.forName("pl.asie.foamfix.client.FastTextureAtlasSprite");
        } catch(ClassNotFoundException e) {
            ffSprite = null;
        }
        FOAMFIX_SPRITE = ffSprite;
    }

    @SubscribeEvent
    public static void registerEvictionListener(ColorHandlerEvent.Block event) {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener((ISelectiveResourceReloadListener) (manager, predicate) -> {
            if (predicate.test(VanillaResourceType.MODELS)) {
                canReload = false;
                Set<Class<?>> skippedSpriteClasses = new HashSet<>();
                try {
                    for (TextureAtlasSprite sprite : ((TextureMapAccessor)Minecraft.getMinecraft().getTextureMapBlocks()).getMapRegisteredSprites().values()) {
                        if (sprite.getClass() == TextureAtlasSprite.class || sprite.getClass() == FOAMFIX_SPRITE) {
                            sprite.setFramesTextureData(new FramesTextureData(sprite));
                        } else
                            skippedSpriteClasses.add(sprite.getClass());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                LoliLogger.instance.info("Evicted most sprites' frame texture data, skipped classes: [{}]", skippedSpriteClasses.stream().map(Class::getName).collect(Collectors.joining(", ")));
                canReload = true;
            }
        });
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            for(TextureAtlasSprite sprite : ((TextureMapAccessor)Minecraft.getMinecraft().getTextureMapBlocks()).getMapRegisteredSprites().values()) {
                if (sprite != null) {
                    List<int[][]> data = ((TextureAtlasSpriteAccessor)sprite).loli$getTextureData();
                    if(data instanceof FramesTextureData)
                        ((FramesTextureData)data).tick();
                }
            }
        }
    }

    private final TextureAtlasSprite sprite;

    private int ticksInactive;

    private static final int INACTIVITY_THRESHOLD = 20;

    public FramesTextureData(TextureAtlasSprite sprite) {
        super();
        this.sprite = sprite;
        this.ticksInactive = INACTIVITY_THRESHOLD + 1;
    }

    public void tick() {
        this.ticksInactive++;
        if(this.ticksInactive == INACTIVITY_THRESHOLD) {
            this.clear();
        }
    }

    @Override
    public int[][] get(int index) {
        if (canReload && super.isEmpty()) {
            load();
        }
        this.ticksInactive = 0;
        return super.get(index);
    }

    @Override
    public int size() {
        if (canReload && super.isEmpty()) {
            load();
        }
        this.ticksInactive = 0;
        return super.size();
    }

    @Override
    public boolean isEmpty() {
        if (canReload && super.isEmpty()) {
            load();
        }
        this.ticksInactive = 0;
        return super.isEmpty();
    }

    @Override
    public void clear() {
        super.clear();
        trimToSize();
    }

    private void load() {
        // prevent recursive loads
        boolean oldReload = canReload;
        canReload = false;
        try {
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
        } finally {
            canReload = oldReload;
        }
    }

    private ResourceLocation getLocation() {
        String[] parts = ResourceLocation.splitObjectName(sprite.getIconName());
        return new ResourceLocation(parts[0], String.format("%s/%s%s", Minecraft.getMinecraft().getTextureMapBlocks().getBasePath(), parts[1], ".png"));
    }
}

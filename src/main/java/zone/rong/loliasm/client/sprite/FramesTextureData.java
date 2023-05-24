package zone.rong.loliasm.client.sprite;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
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
import zone.rong.loliasm.LoliReflector;
import zone.rong.loliasm.common.internal.mixins.TextureAtlasSpriteAccessor;
import zone.rong.loliasm.common.internal.mixins.TextureMapAccessor;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FramesTextureData extends ArrayList<int[][]> {

    private static final Class<?> FOAMFIX_SPRITE = LoliReflector.getNullableClass("pl.asie.foamfix.client.FastTextureAtlasSprite");
    private static final int INACTIVITY_THRESHOLD = 20;

    private static boolean canReload = true;

    @SubscribeEvent
    public static void registerEvictionListener(ColorHandlerEvent.Block event) {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener((ISelectiveResourceReloadListener) (manager, predicate) -> {
            if (predicate.test(VanillaResourceType.MODELS)) {
                canReload = false;
                Set<Class<?>> skippedSpriteClasses = new ObjectOpenHashSet<>();
                try {
                    if (FOAMFIX_SPRITE == null) {
                        for (TextureAtlasSprite sprite : ((TextureMapAccessor) Minecraft.getMinecraft().getTextureMapBlocks()).getMapRegisteredSprites().values()) {
                            if (sprite.getClass() == TextureAtlasSprite.class) {
                                sprite.setFramesTextureData(new FramesTextureData(sprite));
                            } else {
                                skippedSpriteClasses.add(sprite.getClass());
                            }
                        }
                    } else {
                        for (TextureAtlasSprite sprite : ((TextureMapAccessor) Minecraft.getMinecraft().getTextureMapBlocks()).getMapRegisteredSprites().values()) {
                            if (sprite.getClass() == FOAMFIX_SPRITE || sprite.getClass() == TextureAtlasSprite.class) {
                                sprite.setFramesTextureData(new FramesTextureData(sprite));
                            } else {
                                skippedSpriteClasses.add(sprite.getClass());
                            }
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                LoliLogger.instance.debug("Evicted most sprites' frame texture data, skipped classes: [{}]", skippedSpriteClasses.stream().map(Class::getName).collect(Collectors.joining(", ")));
                canReload = true;
            }
        });
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            for (TextureAtlasSprite sprite : ((TextureMapAccessor)Minecraft.getMinecraft().getTextureMapBlocks()).getMapRegisteredSprites().values()) {
                if (sprite != null) {
                    List<int[][]> data = ((TextureAtlasSpriteAccessor) sprite).loli$getTextureData();
                    if (data instanceof FramesTextureData) {
                        ((FramesTextureData) data).tick();
                    }
                }
            }
        }
    }

    private final TextureAtlasSprite sprite;

    private int ticksInactive;

    public FramesTextureData(TextureAtlasSprite sprite) {
        super();
        this.sprite = sprite;
        this.ticksInactive = INACTIVITY_THRESHOLD + 1;
    }

    public void tick() {
        this.ticksInactive++;
        if (this.ticksInactive == INACTIVITY_THRESHOLD) {
            this.clear();
        }
    }

    @Override
    public int[][] get(int index) {
        synchronized (this) {
            if (canReload && super.isEmpty()) {
                load();
            }
            this.ticksInactive = 0;
            return super.get(index);
        }
    }

    @Override
    public int size() {
        synchronized (this) {
            if (canReload && super.isEmpty()) {
                load();
            }
            this.ticksInactive = 0;
            return super.size();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (this) {
            if (canReload && super.isEmpty()) {
                load();
            }
            this.ticksInactive = 0;
            return super.isEmpty();
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            super.clear();
            trimToSize();
        }
    }

    private void load() {
        // Prevent recursive loads
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

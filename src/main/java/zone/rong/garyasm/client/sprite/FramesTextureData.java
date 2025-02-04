package zone.rong.garyasm.client.sprite;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import zone.rong.garyasm.GaryLogger;
import zone.rong.garyasm.GaryReflector;
import zone.rong.garyasm.common.internal.mixins.TextureMapAccessor;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FramesTextureData extends ArrayList<int[][]> {

    private static final Class<?> FOAMFIX_SPRITE = GaryReflector.getNullableClass("pl.asie.foamfix.client.FastTextureAtlasSprite");
    private static final int INACTIVITY_THRESHOLD = 20;

    private static boolean canReload = true;

    private static final Set<FramesTextureData> tickingSpritesSet = new ReferenceLinkedOpenHashSet<>();

    @SubscribeEvent
    public static void registerEvictionListener(ColorHandlerEvent.Block event) {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener((ISelectiveResourceReloadListener) (manager, predicate) -> {
            if (predicate.test(VanillaResourceType.MODELS)) {
                canReload = false;
                int count = 0;
                Set<Class<?>> skippedSpriteClasses = new ObjectOpenHashSet<>();
                try {
                    synchronized (tickingSpritesSet) {
                        tickingSpritesSet.clear();
                    }
                    for (TextureAtlasSprite sprite : ((TextureMapAccessor) Minecraft.getMinecraft().getTextureMapBlocks()).getMapRegisteredSprites().values()) {
                        if (!sprite.hasAnimationMetadata()) {
                            if (sprite.getClass() == FOAMFIX_SPRITE || sprite.getClass() == TextureAtlasSprite.class) {
                                count++;
                                sprite.setFramesTextureData(new FramesTextureData(sprite));
                            } else {
                                skippedSpriteClasses.add(sprite.getClass());
                            }
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                GaryLogger.instance.info("Evicted {} sprites' frame texture data", count);
                GaryLogger.instance.debug("While evicting sprites' frame texture data, the following classes were skipped: [{}]", skippedSpriteClasses.stream().map(Class::getName).collect(Collectors.joining(", ")));
                canReload = true;
            }
        });
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            List<FramesTextureData> toTick;
            synchronized (tickingSpritesSet) {
                toTick = new ArrayList<>(tickingSpritesSet);
            }
            for(FramesTextureData data : toTick) {
                if(data.tick()) {
                    synchronized (tickingSpritesSet) {
                        tickingSpritesSet.remove(data);
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

    public boolean tick() {
        synchronized (this) {
            this.ticksInactive++;
            if (this.ticksInactive == INACTIVITY_THRESHOLD) {
                this.clear();
                return true;
            }
            return false;
        }
    }

    private void markActive() {
        this.ticksInactive = 0;
        synchronized (tickingSpritesSet) {
            tickingSpritesSet.add(this);
        }
    }

    @Override
    public int[][] get(int index) {
        synchronized (this) {
            if (canReload && super.isEmpty()) {
                load();
            }
            markActive();
            return super.get(index);
        }
    }

    @Override
    public int size() {
        synchronized (this) {
            if (canReload && super.isEmpty()) {
                load();
            }
            markActive();
            return super.size();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (this) {
            if (canReload && super.isEmpty()) {
                load();
            }
            markActive();
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
                int mipLevels = Minecraft.getMinecraft().gameSettings.mipmapLevels;
                try (IResource resource = resourceManager.getResource(location)) {
                    sprite.loadSpriteFrames(resource, mipLevels + 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                /* generate mipmaps, as loadSpriteFrames doesn't actually fill them in */
                for (int i = 0; i < this.size(); i++) {
                    int[][] aint = this.get(i);
                    if (aint != null) {
                        this.set(i, TextureUtil.generateMipmapData(mipLevels, sprite.getIconWidth(), aint));
                    }
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

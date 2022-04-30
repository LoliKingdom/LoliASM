package zone.rong.loliasm.client.sprite.ondemand.mixins;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.*;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.client.sprite.ondemand.IAnimatedSpriteActivator;
import zone.rong.loliasm.client.sprite.ondemand.IAnimatedSpritePrimer;
import zone.rong.loliasm.client.sprite.ondemand.IBufferPrimerConfigurator;
import zone.rong.loliasm.client.sprite.ondemand.ICompiledChunkExpander;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

@Mixin(TextureMap.class)
public abstract class TextureMapMixin extends AbstractTexture implements IAnimatedSpritePrimer {

    @Shadow @Final public Map<String, TextureAtlasSprite> mapUploadedSprites;

    @Unique private final TreeMap<Float, TreeMap<Float, TextureAtlasSprite>> animatedSpritesUVRanges = new TreeMap<>();
    @Unique private final Set<Pair<Float, Float>> queuedUVCoords = new ObjectOpenHashSet<>(8);

    @Override
    public void registerUVRanges(float minU, float minV, TextureAtlasSprite sprite) {
        animatedSpritesUVRanges.computeIfAbsent(minU, k -> new TreeMap<>()).put(minV, sprite);
    }

    @Override
    public void addAnimatedSprite(float u, float v) {
        this.queuedUVCoords.add(Pair.of(u, v));
    }

    @Nullable
    @Override
    public TextureAtlasSprite getAnimatedSprite(float u, float v) {
        Entry<Float, TreeMap<Float, TextureAtlasSprite>> vMapping = animatedSpritesUVRanges.floorEntry(u);
        if (vMapping != null) {
            Entry<Float, TextureAtlasSprite> uMapping = vMapping.getValue().floorEntry(v);
            return uMapping == null ? null : uMapping.getValue();
        }
        return null;
    }

    @Inject(method = "<init>(Ljava/lang/String;Lnet/minecraft/client/renderer/texture/ITextureMapPopulator;Z)V", at = @At("RETURN"))
    private void afterInit(String basePathIn, ITextureMapPopulator iconCreatorIn, boolean skipFirst, CallbackInfo ci) {
        ((IBufferPrimerConfigurator) Tessellator.getInstance().getBuffer()).setPrimer(this);
    }

    @SuppressWarnings("all")
    @Redirect(method = "finishLoading", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false), remap = false)
    private boolean populateUVRanges(List list, Object object) {
        TextureAtlasSprite sprite = (TextureAtlasSprite) object;
        registerUVRanges(sprite.getMinU(), sprite.getMinV(), sprite);
        return false;
    }

    /**
     * @author Rongmario
     * @reason Custom updateAnimations logic
     */
    @Overwrite
    public void updateAnimations() {
        if ((Object) this == Minecraft.getMinecraft().getTextureMapBlocks()) {
            for (Object info : ((RenderGlobalAccessor) Minecraft.getMinecraft().renderGlobal).getRenderInfos()) {
                for (TextureAtlasSprite sprite : ((ICompiledChunkExpander) ((ContainerLocalRenderInformationAccessor) info).getRenderChunk().compiledChunk).getVisibleTextures()) {
                    ((IAnimatedSpriteActivator) sprite).setActive(true);
                }
            }

            Iterator<Pair<Float, Float>> iter = this.queuedUVCoords.iterator();
            while (iter.hasNext()) {
                Pair<Float, Float> pair = iter.next();
                TextureAtlasSprite sprite = getAnimatedSprite(pair.getLeft(), pair.getRight());
                if (sprite != null && sprite.hasAnimationMetadata()) {
                    ((IAnimatedSpriteActivator) sprite).setActive(true);
                }
                iter.remove();
            }

            GlStateManager.bindTexture(this.getGlTextureId());

            for (TextureAtlasSprite sprite : this.mapUploadedSprites.values()) {
                if (((IAnimatedSpriteActivator) sprite).isActive()) {
                    sprite.updateAnimation();
                    ((IAnimatedSpriteActivator) sprite).setActive(false);
                }
            }

            /*
            for (TextureAtlasSprite sprite : this.listAnimatedSprites) {
                if (((IAnimatedSpriteActivator) sprite).isActive()) {
                    sprite.updateAnimation();
                    ((IAnimatedSpriteActivator) sprite).setActive(false);
                }
            }
             */

            // this.listAnimatedSprites.clear();
        }
    }

}

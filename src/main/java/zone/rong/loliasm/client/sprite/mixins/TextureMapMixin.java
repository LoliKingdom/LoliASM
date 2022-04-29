package zone.rong.loliasm.client.sprite.mixins;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import mezz.jei.gui.textures.JeiTextureMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ITextureMapPopulator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.client.sprite.IAnimatedSpritePrimer;
import zone.rong.loliasm.client.sprite.IBufferPrimerConfigurator;
import zone.rong.loliasm.client.sprite.ICompiledChunkExpander;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

@Mixin(TextureMap.class)
public abstract class TextureMapMixin extends AbstractTexture implements IAnimatedSpritePrimer {

    @Shadow @Final protected List<TextureAtlasSprite> listAnimatedSprites;

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

    @Redirect(method = "finishLoading", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;hasAnimationMetadata()Z"))
    private boolean populateAnimatedSprites(TextureAtlasSprite sprite) {
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
            Set<TextureAtlasSprite> queue = new ReferenceOpenHashSet<>(32);
            for (Object info : ((RenderGlobalAccessor) Minecraft.getMinecraft().renderGlobal).getRenderInfos()) {
                ICompiledChunkExpander chunk = (ICompiledChunkExpander) ((ContainerLocalRenderInformationAccessor) info).getRenderChunk().compiledChunk;
                queue.addAll(chunk.getVisibleTextures());
                /*
                if (chunk.getVisibleTexturesCoords() != null && !chunk.getVisibleTexturesCoords().isEmpty()) {
                    if (chunk.getVisibleTextures() == null) {
                        Set<TextureAtlasSprite> resolved = new ReferenceOpenHashSet<>(chunk.getVisibleTexturesCoords().size());
                        for (Pair<Float, Float> uv : chunk.getVisibleTexturesCoords()) {
                            TextureAtlasSprite sprite = getAnimatedSprite(uv.getLeft(), uv.getRight());
                            if (sprite != null && sprite.hasAnimationMetadata()) {
                                resolved.add(sprite);
                            }
                        }
                        chunk.resolve(resolved);
                    }
                }
                if (chunk.getVisibleTextures() != null) {
                    this.listAnimatedSprites.addAll(chunk.getVisibleTextures());
                }
                 */
            }

            GlStateManager.bindTexture(this.getGlTextureId());

            Iterator<Pair<Float, Float>> iter = this.queuedUVCoords.iterator();
            while (iter.hasNext()) {
                Pair<Float, Float> pair = iter.next();
                TextureAtlasSprite sprite = getAnimatedSprite(pair.getLeft(), pair.getRight());
                if (sprite != null && sprite.hasAnimationMetadata()) {
                    queue.add(sprite);
                }
                iter.remove();
            }

            for (TextureAtlasSprite textureAtlasSprite : queue) {
                textureAtlasSprite.updateAnimation();
            }
        }
    }

}

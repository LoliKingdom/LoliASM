package zone.rong.blahajasm.client.sprite.ondemand.mixins;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.blahajasm.client.sprite.ondemand.*;

import javax.annotation.Nullable;
import java.util.*;

@Mixin(TextureMap.class)
public abstract class TextureMapMixin extends AbstractTexture implements IAnimatedSpritePrimer {

    @Shadow @Final private List<TextureAtlasSprite> listAnimatedSprites;

    @Unique private final FloorUVTree animatedSpritesUVRanges = new FloorUVTree();
    @Unique private final Set<FloorUV> queuedUVCoords = new ObjectLinkedOpenHashSet<>(8);

    @Override
    public void registerUVRanges(float minU, float minV, TextureAtlasSprite sprite) {
        animatedSpritesUVRanges.put(minU, minV, sprite);
    }

    @Override
    public void addAnimatedSprite(float u, float v) {
        this.queuedUVCoords.add(FloorUV.of(u, v));
    }

    @Nullable
    @Override
    public TextureAtlasSprite getAnimatedSprite(float u, float v) {
        return this.animatedSpritesUVRanges.getNearestFloorSprite(u, v);
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
        return listAnimatedSprites.add(sprite);
    }

    /**
     * @author Rongmario
     * @reason Custom updateAnimations logic
     */
    @Overwrite
    public void updateAnimations() {
        if ((Object) this == Minecraft.getMinecraft().getTextureMapBlocks()) { // Only run once, in main TextureMap, not in its subclasses

            // Activate all animated sprites send from the block, fluid renderer in various render worker threads
            for (Object info : ((RenderGlobalAccessor) Minecraft.getMinecraft().renderGlobal).getRenderInfos()) {
                for (TextureAtlasSprite sprite : ((ICompiledChunkExpander) ((ContainerLocalRenderInformationAccessor) info).getRenderChunk().compiledChunk).getVisibleTextures()) {
                    ((IAnimatedSpriteActivator) sprite).setActive(true);
                }
            }

            // Mark all captured sprites
            for (FloorUV queuedUVCoord : this.queuedUVCoords) {
                TextureAtlasSprite sprite = this.animatedSpritesUVRanges.getNearestFloorSprite(queuedUVCoord);
                if (sprite != null && sprite.hasAnimationMetadata()) { // Only activate animated sprites
                    ((IAnimatedSpriteActivator) sprite).setActive(true);
                }
            }
            this.queuedUVCoords.clear();

            GlStateManager.bindTexture(this.getGlTextureId()); // Bind TextureMap texture

            for (TextureAtlasSprite sprite : this.listAnimatedSprites) {
                if (((IAnimatedSpriteActivator) sprite).isActive()) {
                    sprite.updateAnimation(); // Update Animation
                    ((IAnimatedSpriteActivator) sprite).setActive(false); // Unactivated
                }
            }

        }
    }

}

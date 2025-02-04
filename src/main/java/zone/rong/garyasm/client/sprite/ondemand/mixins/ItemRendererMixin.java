package zone.rong.garyasm.client.sprite.ondemand.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.garyasm.client.sprite.ondemand.IAnimatedSpriteActivator;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Shadow @Final public Minecraft mc;

    @Inject(method = "renderSuffocationOverlay", at = @At("HEAD"))
    private void beforeRenderBlockInHand(TextureAtlasSprite texture, CallbackInfo ci) {
        if (texture.hasAnimationMetadata()) {
            ((IAnimatedSpriteActivator) texture).setActive(true);
        }
    }

    @Inject(method = "renderFireInFirstPerson", at = @At("HEAD"))
    private void beforeRenderFireInFirstPerson(CallbackInfo ci) {
        // TODO: cache "minecraft:blocks/fire_layer_1", and refresh on RenderGlobal#onResourceManagerReload
        ((IAnimatedSpriteActivator) mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/fire_layer_1")).setActive(true);
    }

}

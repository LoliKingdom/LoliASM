package zone.rong.blahajasm.client.sprite.ondemand.mixins;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import zone.rong.blahajasm.client.sprite.ondemand.IAnimatedSpriteActivator;

import java.util.List;

@Mixin(RenderItem.class)
public class RenderItemMixin {

    @Inject(method = "renderQuads", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/model/pipeline/LightUtil;renderQuadColor(Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/client/renderer/block/model/BakedQuad;I)V"),
    locals = LocalCapture.CAPTURE_FAILHARD)
    public void beforeRenderItem(BufferBuilder renderer, List<BakedQuad> quads, int color, ItemStack stack, CallbackInfo ci, boolean flag, int i, int j, BakedQuad bakedquad) {
        TextureAtlasSprite sprite = bakedquad.getSprite();
        if (sprite != null && sprite.hasAnimationMetadata()) {
            ((IAnimatedSpriteActivator) bakedquad.getSprite()).setActive(true);
        }
    }

}

package zone.rong.loliasm.client.sprite.ondemand.mixins;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.client.sprite.ondemand.IAnimatedSpritePrimer;

@Mixin(TextureManager.class)
public class TextureManagerMixin {

    @Inject(method = "bindTexture", at = @At("HEAD"))
    private void checkForMainSpriteSheet(ResourceLocation resource, CallbackInfo ci) {
        if (resource == TextureMap.LOCATION_BLOCKS_TEXTURE) {
            IAnimatedSpritePrimer.PRIMED.set(true);
        }
    }

}

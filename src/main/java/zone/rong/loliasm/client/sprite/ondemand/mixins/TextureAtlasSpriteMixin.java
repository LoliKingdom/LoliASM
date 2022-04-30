package zone.rong.loliasm.client.sprite.ondemand.mixins;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import zone.rong.loliasm.client.sprite.ondemand.IAnimatedSpriteActivator;

@Mixin(TextureAtlasSprite.class)
public class TextureAtlasSpriteMixin implements IAnimatedSpriteActivator {

    @Unique private boolean loli$active;

    @Override
    public boolean isActive() {
        return loli$active;
    }

    @Override
    public void setActive(boolean active) {
        this.loli$active = active;
    }

}

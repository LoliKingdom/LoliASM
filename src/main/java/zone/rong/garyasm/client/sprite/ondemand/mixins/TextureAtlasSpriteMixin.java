package zone.rong.garyasm.client.sprite.ondemand.mixins;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import zone.rong.garyasm.client.sprite.ondemand.IAnimatedSpriteActivator;

@Mixin(TextureAtlasSprite.class)
public class TextureAtlasSpriteMixin implements IAnimatedSpriteActivator {

    @Unique private boolean gary$active;

    @Override
    public boolean isActive() {
        return gary$active;
    }

    @Override
    public void setActive(boolean active) {
        this.gary$active = active;
    }

}

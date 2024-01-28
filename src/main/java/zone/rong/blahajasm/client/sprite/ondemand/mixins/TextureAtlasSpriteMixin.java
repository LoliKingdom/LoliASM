package zone.rong.blahajasm.client.sprite.ondemand.mixins;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import zone.rong.blahajasm.client.sprite.ondemand.IAnimatedSpriteActivator;

@Mixin(TextureAtlasSprite.class)
public class TextureAtlasSpriteMixin implements IAnimatedSpriteActivator {

    @Unique private boolean blahaj$active;

    @Override
    public boolean isActive() {
        return blahaj$active;
    }

    @Override
    public void setActive(boolean active) {
        this.blahaj$active = active;
    }

}

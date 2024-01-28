package zone.rong.blahajasm.common.internal.mixins;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(TextureAtlasSprite.class)
public interface TextureAtlasSpriteAccessor {

    @Accessor("framesTextureData")
    List<int[][]> blahaj$getTextureData();

}

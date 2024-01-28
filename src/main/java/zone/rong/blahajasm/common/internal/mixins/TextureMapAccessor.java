package zone.rong.blahajasm.common.internal.mixins;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TextureMap.class)
public interface TextureMapAccessor {

    @Accessor("mapRegisteredSprites")
    Map<String, TextureAtlasSprite> getMapRegisteredSprites();

}

package zone.rong.loliasm.client.sprite.ondemand;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Set;

public interface ICompiledChunkExpander {

    @Nullable
    @Deprecated
    default Set<Pair<Float, Float>> getVisibleTexturesCoords() {
        return null;
    }

    Set<TextureAtlasSprite> getVisibleTextures();

    @Deprecated
    default void resolve(Set<TextureAtlasSprite> visibleTextures) {

    }

    void resolve(TextureAtlasSprite sprite);

}

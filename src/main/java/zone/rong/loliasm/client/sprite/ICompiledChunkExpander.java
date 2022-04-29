package zone.rong.loliasm.client.sprite;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Set;

public interface ICompiledChunkExpander {

    @Nullable
    Set<Pair<Float, Float>> getVisibleTexturesCoords();

    @Nullable
    Set<TextureAtlasSprite> getVisibleTextures();

    void resolve(Set<TextureAtlasSprite> visibleTextures);

    void resolve(TextureAtlasSprite sprite);

}

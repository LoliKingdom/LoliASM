package zone.rong.loliasm.client.sprite.ondemand;

import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import javax.annotation.Nullable;

public interface IAnimatedSpritePrimer {

    ThreadLocal<CompiledChunk> CURRENT_COMPILED_CHUNK = ThreadLocal.withInitial(() -> CompiledChunk.DUMMY);

    ThreadLocal<Boolean> PRIMED = ThreadLocal.withInitial(() -> false);

    void addAnimatedSprite(float u, float v);

    default void registerUVRanges(float minU, float minV, TextureAtlasSprite sprite) { }

    @Nullable default TextureAtlasSprite getAnimatedSprite(float u, float v) {
        return null;
    }

}

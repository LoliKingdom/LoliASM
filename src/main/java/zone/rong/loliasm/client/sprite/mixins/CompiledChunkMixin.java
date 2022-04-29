package zone.rong.loliasm.client.sprite.mixins;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import zone.rong.loliasm.client.sprite.IAnimatedSpritePrimer;
import zone.rong.loliasm.client.sprite.ICompiledChunkExpander;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(CompiledChunk.class)
public class CompiledChunkMixin implements ICompiledChunkExpander, IAnimatedSpritePrimer {

    // @Unique private Set<Pair<Float, Float>> visibleTexturesCoords = new ObjectOpenHashSet<>(8);

    // @Unique private Set<TextureAtlasSprite> visibleTexturesBuilt;

    @Unique private final Set<TextureAtlasSprite> visibleTexturesBuilt = new ReferenceOpenHashSet<>(32);

    @Override
    public Set<Pair<Float, Float>> getVisibleTexturesCoords() {
        // return visibleTexturesCoords;
        return null;
    }

    @Override
    @Nullable
    public Set<TextureAtlasSprite> getVisibleTextures() {
        return visibleTexturesBuilt;
    }

    @Override
    public void resolve(Set<TextureAtlasSprite> visibleTextures) {
        // this.visibleTexturesCoords = null;
        // this.visibleTexturesBuilt = visibleTextures;
    }

    @Override
    public void resolve(TextureAtlasSprite sprite) {
        this.visibleTexturesBuilt.add(sprite);
    }

    @Override
    public void addAnimatedSprite(float u, float v) {
        // this.visibleTexturesCoords.add(Pair.of(u, v));
    }

}

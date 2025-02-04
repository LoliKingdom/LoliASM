package zone.rong.garyasm.client.sprite.ondemand.mixins;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import zone.rong.garyasm.client.sprite.ondemand.ICompiledChunkExpander;

import java.util.Set;

@Mixin(CompiledChunk.class)
public class CompiledChunkMixin implements ICompiledChunkExpander {

    @Unique private final Set<TextureAtlasSprite> visibleTextures = new ReferenceOpenHashSet<>(16);

    @Override
    public Set<TextureAtlasSprite> getVisibleTextures() {
        return visibleTextures;
    }

    @Override
    public void resolve(TextureAtlasSprite sprite) {
        this.visibleTextures.add(sprite);
    }

}

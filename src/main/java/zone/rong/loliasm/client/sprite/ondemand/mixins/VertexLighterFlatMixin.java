package zone.rong.loliasm.client.sprite.ondemand.mixins;

import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import zone.rong.loliasm.client.sprite.ondemand.IAnimatedSpritePrimer;
import zone.rong.loliasm.client.sprite.ondemand.ICompiledChunkExpander;
import zone.rong.loliasm.client.sprite.ondemand.IVertexLighterExpander;

@Mixin(VertexLighterFlat.class)
public abstract class VertexLighterFlatMixin extends QuadGatheringTransformer implements IVertexLighterExpander {

    @Unique private boolean primedForDispatch = false;

    @Override
    public Object primeForDispatch() {
        this.primedForDispatch = true;
        return this;
    }

    @Override
    public void setTexture(TextureAtlasSprite texture) {
        if (this.primedForDispatch && texture.hasAnimationMetadata()) {
            CompiledChunk chunk = IAnimatedSpritePrimer.CURRENT_COMPILED_CHUNK.get();
            if (chunk != null) {
                ((ICompiledChunkExpander) chunk).resolve(texture);
            }
        }
    }

}

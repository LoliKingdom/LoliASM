package zone.rong.loliasm.client.sprite.ondemand.mixins;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import zone.rong.loliasm.client.sprite.ondemand.IAnimatedSpritePrimer;
import zone.rong.loliasm.client.sprite.ondemand.ICompiledChunkExpander;

import java.util.BitSet;
import java.util.List;

@Mixin(BlockModelRenderer.class)
public class BlockModelRendererMixin {

    @Redirect(method = "renderQuadsSmooth", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", remap = false))
    private Object sendAnimatedSprites$smooth(List list, int i) {
        BakedQuad bakedquad = (BakedQuad) list.get(i);
        if (bakedquad.getSprite().hasAnimationMetadata()) {
            CompiledChunk chunk = IAnimatedSpritePrimer.CURRENT_COMPILED_CHUNK.get();
            if (chunk != CompiledChunk.DUMMY) {
                ((ICompiledChunkExpander) chunk).resolve(bakedquad.getSprite());
            }
        }
        return bakedquad;
    }

    @SuppressWarnings("all")
    @Redirect(method = "renderQuadsFlat", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0))
    private BakedQuad sendAnimatedSprites$flat(List<BakedQuad> list, int i) {
        BakedQuad bakedquad = list.get(i);
        if (bakedquad.getSprite().hasAnimationMetadata()) {
            CompiledChunk chunk = IAnimatedSpritePrimer.CURRENT_COMPILED_CHUNK.get();
            if (chunk != CompiledChunk.DUMMY) {
                ((ICompiledChunkExpander) chunk).resolve(bakedquad.getSprite());
            }
        }
        return bakedquad;
    }

}

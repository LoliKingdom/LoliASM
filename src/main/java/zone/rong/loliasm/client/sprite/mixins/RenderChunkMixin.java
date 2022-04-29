package zone.rong.loliasm.client.sprite.mixins;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.client.sprite.IAnimatedSpritePrimer;
import zone.rong.loliasm.client.sprite.IBufferPrimerConfigurator;

@Mixin(RenderChunk.class)
public class RenderChunkMixin {

    @Unique private ThreadLocal<ChunkCompileTaskGenerator> generator;

    @Inject(method = "rebuildChunk", at = @At("HEAD"))
    private void preparePreRender(float x, float y, float z, ChunkCompileTaskGenerator generator, CallbackInfo ci) {
        this.generator = ThreadLocal.withInitial(() -> generator);
    }

    @Inject(method = "rebuildChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/CompiledChunk;setVisibility(Lnet/minecraft/client/renderer/chunk/SetVisibility;)V"))
    private void preparePostRender(float x, float y, float z, ChunkCompileTaskGenerator generator, CallbackInfo ci) {
        this.generator = null;
        IAnimatedSpritePrimer.PRIMED.set(false);
    }

    /**
     * @author Rongmario
     * @reason
     */
    @Overwrite
    private void preRenderBlocks(BufferBuilder bufferBuilder, BlockPos pos) {
        bufferBuilder.begin(7, DefaultVertexFormats.BLOCK);
        bufferBuilder.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
        if (this.generator != null) {
            IAnimatedSpritePrimer.PRIMED.set(true);
            ((IBufferPrimerConfigurator) bufferBuilder).setPrimer((IAnimatedSpritePrimer) this.generator.get().getCompiledChunk());
        }
    }

}

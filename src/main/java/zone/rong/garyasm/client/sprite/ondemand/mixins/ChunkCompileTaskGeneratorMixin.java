package zone.rong.garyasm.client.sprite.ondemand.mixins;

import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator.Type;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import zone.rong.garyasm.client.sprite.ondemand.IAnimatedSpritePrimer;

@Mixin(ChunkCompileTaskGenerator.class)
public class ChunkCompileTaskGeneratorMixin {

    @Shadow @Final private Type type;
    
    @Shadow private CompiledChunk compiledChunk;

    /**
     * @author Rongmario
     * @reason Prime the current CompiledChunk to the ThreadLocal variable
     */
    @Overwrite
    public void setCompiledChunk(CompiledChunk compiledChunk) {
        this.compiledChunk = compiledChunk;
        if (this.type == Type.REBUILD_CHUNK) {
            IAnimatedSpritePrimer.CURRENT_COMPILED_CHUNK.set(compiledChunk);
        }
    }

}

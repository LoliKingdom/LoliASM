package zone.rong.loliasm.client.sprite;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;

public class VertexBufferConsumerWrappedSpriteDispatcher extends VertexBufferConsumer {

    @Override
    public void setTexture(TextureAtlasSprite texture) {
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread() && texture.hasAnimationMetadata()) {
            CompiledChunk chunk = IAnimatedSpritePrimer.CURRENT_COMPILED_CHUNK.get();
            if (chunk != CompiledChunk.DUMMY) {
                ((ICompiledChunkExpander) chunk).resolve(texture);
            }
        }
    }

}

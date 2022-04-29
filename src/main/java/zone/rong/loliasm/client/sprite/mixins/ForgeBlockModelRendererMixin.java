package zone.rong.loliasm.client.sprite.mixins;

import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import zone.rong.loliasm.client.sprite.VertexBufferConsumerWrappedSpriteDispatcher;

@Mixin(value = ForgeBlockModelRenderer.class, remap = false)
public class ForgeBlockModelRendererMixin {

    @Shadow @Final @Mutable private ThreadLocal<VertexBufferConsumer> consumerFlat = ThreadLocal.withInitial(VertexBufferConsumerWrappedSpriteDispatcher::new);
    @Shadow @Final @Mutable private ThreadLocal<VertexBufferConsumer> consumerSmooth = ThreadLocal.withInitial(VertexBufferConsumerWrappedSpriteDispatcher::new);

}

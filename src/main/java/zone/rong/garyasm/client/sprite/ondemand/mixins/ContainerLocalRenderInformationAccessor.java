package zone.rong.garyasm.client.sprite.ondemand.mixins;

import net.minecraft.client.renderer.chunk.RenderChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.renderer.RenderGlobal$ContainerLocalRenderInformation")
public interface ContainerLocalRenderInformationAccessor {

    @Accessor
    RenderChunk getRenderChunk();

}

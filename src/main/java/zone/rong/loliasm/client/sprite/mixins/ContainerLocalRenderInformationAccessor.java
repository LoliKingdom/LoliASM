package zone.rong.loliasm.client.sprite.mixins;

import net.minecraft.client.renderer.chunk.RenderChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.renderer.RenderGlobal$ContainerLocalRenderInformation")
public interface ContainerLocalRenderInformationAccessor {

    @Accessor
    RenderChunk getRenderChunk();

}

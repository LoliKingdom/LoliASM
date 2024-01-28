package zone.rong.blahajasm.client.sprite.ondemand.mixins;

import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(RenderGlobal.class)
public interface RenderGlobalAccessor {

    @Accessor
    List getRenderInfos();

}

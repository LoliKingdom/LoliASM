package zone.rong.garyasm.common.singletonevents.mixins.gameevent;

import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import zone.rong.garyasm.common.singletonevents.IRefreshEvent;

@Mixin(RenderTickEvent.class)
public class RenderTickEventMixin implements IRefreshEvent {

    @Shadow @Final @Mutable public float renderTickTime;

    @Override
    public void beforeRenderTick(float timer) {
        Animation.setClientPartialTickTime(timer);
        this.renderTickTime = timer;
    }

}

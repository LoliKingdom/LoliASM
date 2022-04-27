package zone.rong.loliasm.common.singletonevents.mixins.gameevent;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.asm.mixin.*;
import zone.rong.loliasm.common.singletonevents.IRefreshEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mixin(value = TickEvent.class, remap = false)
public class TickEventMixin extends Event implements IRefreshEvent {

    @Shadow @Final @Mutable public Side side;

    @Unique private EventPriority loliPriority = null;

    @Override
    public IRefreshEvent setTickSide(Side side) {
        this.side = side;
        return this;
    }

    @Nullable
    @Override
    public EventPriority getPhase() {
        return loliPriority;
    }

    @Override
    public void setPhase(@Nonnull EventPriority next) {
        this.loliPriority = next;
    }

}

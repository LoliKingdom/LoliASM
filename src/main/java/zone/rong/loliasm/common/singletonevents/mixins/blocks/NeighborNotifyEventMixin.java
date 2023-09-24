package zone.rong.loliasm.common.singletonevents.mixins.blocks;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import org.spongepowered.asm.mixin.*;
import zone.rong.loliasm.common.singletonevents.IRefreshEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

@Mixin(NeighborNotifyEvent.class)
public class NeighborNotifyEventMixin extends Event implements IRefreshEvent {

    @Shadow @Final @Mutable private EnumSet<EnumFacing> notifiedSides;
    @Shadow @Final @Mutable private boolean forceRedstoneUpdate;

    @Unique private EventPriority loliPriority = null;

    @Override
    public void beforeNeighborNotify(EnumSet<EnumFacing> notifiedSides, boolean forceRedstoneUpdate) {
        this.notifiedSides = notifiedSides;
        this.forceRedstoneUpdate = forceRedstoneUpdate;
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

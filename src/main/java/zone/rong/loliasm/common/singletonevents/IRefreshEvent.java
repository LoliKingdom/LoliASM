package zone.rong.loliasm.common.singletonevents;

import com.google.common.base.Preconditions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import java.util.EnumSet;

/**
 * Events will gather this and use as their new values before firing on the EventBus
 *
 * All {@link net.minecraftforge.fml.common.eventhandler.Event} implementing this must:
 *
 * 1. Have a unique field: { private EventPriority loliPriority = null; }
 * 2. Override {@link Event#getPhase()} and {@link Event#setPhase(EventPriority)}
 * 3. Point loliPriority towards those getters/setters
 * 4. Check Preconditions in {@link Event#setPhase(EventPriority)} via {@link IRefreshEvent#checkPrecondition(EventPriority, EventPriority)}
 */
public interface IRefreshEvent {

    // TODO: evaluate if we need to check preconditions
    @Deprecated
    static void checkPrecondition(EventPriority current, EventPriority next) {
        Preconditions.checkNotNull(next, "setPhase argument must not be null");
        Preconditions.checkArgument((current == null ? -1 : current.ordinal()) < next.ordinal(), "Attempted to set event phase to %s when already %s", next, current);
    }

    default void refreshAttachCapabilities(Object data) {

    }

    default void refreshBlockEvent(World world, BlockPos pos, IBlockState state) {

    }

    default void refreshNeighborNotify(EnumSet<EnumFacing> notifiedSides, boolean forceRedstoneUpdate) {

    }

}

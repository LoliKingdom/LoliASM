package zone.rong.loliasm.common.singletonevents;

import com.google.common.base.Preconditions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.relauncher.Side;

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

    default void beforeAttachCapabilities(Object data) {

    }

    default void beforeBlockEvent(World world, BlockPos pos, IBlockState state) {

    }

    default void beforeNeighborNotify(EnumSet<EnumFacing> notifiedSides, boolean forceRedstoneUpdate) {

    }

    // Special-case for PlayerTickEvents
    default IRefreshEvent setTickSide(Side side) {
        return this;
    }

    default void beforeWorldTick(World world) {

    }

    // TODO: determine if this avoiding this will memory leak
    default void afterWorldTick() {

    }

    default void beforePlayerTick(EntityPlayer player) {

    }

    // TODO: determine if this avoiding this will memory leak
    default void afterPlayerTick() {

    }

    default void beforeRenderTick(float timer) {

    }

}

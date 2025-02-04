package zone.rong.garyasm.common.singletonevents.mixins.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import org.spongepowered.asm.mixin.*;
import zone.rong.garyasm.common.singletonevents.IRefreshEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.EnumSet;

@Mixin(NeighborNotifyEvent.class)
public class NeighborNotifyEventMixin extends BlockEvent implements IRefreshEvent {

    @Shadow @Final @Mutable private EnumSet<EnumFacing> notifiedSides;
    @Shadow @Final @Mutable private boolean forceRedstoneUpdate;

    @Unique private EventPriority garyPriority;
    @Unique private WeakReference<World> garyWorldRef;
    @Unique private BlockPos garyPos;
    @Unique private IBlockState garyState;

    NeighborNotifyEventMixin(World world, BlockPos pos, IBlockState state) {
        super(world, pos, state);
        throw new AssertionError();
    }

    @Override
    public World getWorld() {
        return this.garyWorldRef.get();
    }

    @Override
    public BlockPos getPos() {
        return garyPos;
    }

    @Override
    public IBlockState getState() {
        return garyState;
    }

    @Override
    public void beforeNeighborNotify(World world, BlockPos pos, IBlockState state, EnumSet<EnumFacing> notifiedSides, boolean forceRedstoneUpdate) {
        this.garyWorldRef = new WeakReference<>(world);
        this.garyPos = pos;
        this.garyState = state;
        this.notifiedSides = notifiedSides;
        this.forceRedstoneUpdate = forceRedstoneUpdate;
    }

    @Nullable
    @Override
    public EventPriority getPhase() {
        return garyPriority;
    }

    @Override
    public void setPhase(@Nonnull EventPriority next) {
        this.garyPriority = next;
    }

}

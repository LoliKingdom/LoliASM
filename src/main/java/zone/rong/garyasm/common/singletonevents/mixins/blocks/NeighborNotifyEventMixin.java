package zone.rong.loliasm.common.singletonevents.mixins.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import org.spongepowered.asm.mixin.*;
import zone.rong.loliasm.common.singletonevents.IRefreshEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.EnumSet;

@Mixin(NeighborNotifyEvent.class)
public class NeighborNotifyEventMixin extends BlockEvent implements IRefreshEvent {

    @Shadow @Final @Mutable private EnumSet<EnumFacing> notifiedSides;
    @Shadow @Final @Mutable private boolean forceRedstoneUpdate;

    @Unique private EventPriority loliPriority;
    @Unique private WeakReference<World> loliWorldRef;
    @Unique private BlockPos loliPos;
    @Unique private IBlockState loliState;

    NeighborNotifyEventMixin(World world, BlockPos pos, IBlockState state) {
        super(world, pos, state);
        throw new AssertionError();
    }

    @Override
    public World getWorld() {
        return this.loliWorldRef.get();
    }

    @Override
    public BlockPos getPos() {
        return loliPos;
    }

    @Override
    public IBlockState getState() {
        return loliState;
    }

    @Override
    public void beforeNeighborNotify(World world, BlockPos pos, IBlockState state, EnumSet<EnumFacing> notifiedSides, boolean forceRedstoneUpdate) {
        this.loliWorldRef = new WeakReference<>(world);
        this.loliPos = pos;
        this.loliState = state;
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

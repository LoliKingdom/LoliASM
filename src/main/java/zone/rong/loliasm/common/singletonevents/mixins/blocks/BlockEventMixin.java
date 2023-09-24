package zone.rong.loliasm.common.singletonevents.mixins.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.*;
import zone.rong.loliasm.common.singletonevents.IRefreshEvent;

import java.lang.ref.WeakReference;

@Mixin(value = BlockEvent.class, remap = false)
public class BlockEventMixin extends Event implements IRefreshEvent {

    // @Shadow @Final @Mutable private World world;
    @Shadow @Final @Mutable private BlockPos pos;
    @Shadow @Final @Mutable private IBlockState state;

    @Unique private WeakReference<World> loliWorldRef;

    @Override
    public void beforeBlockEvent(World world, BlockPos pos, IBlockState state) {
        // this.world = world;
        this.loliWorldRef = new WeakReference<>(world);
        this.pos = pos;
        this.state = state;
    }

    @Overwrite
    public World getWorld() {
        return this.loliWorldRef.get();
    }

}

package zone.rong.garyasm.common.efficienthashing.mixins;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Thanks to
 * @author PrototypeTrousers: for bringing attention to this issue: https://i.imgur.com/RctqPTl.png
 * @author Baritone: for the hashCode implemntation. It beats out vanilla hashCode implementation especially when collisions occur (20%!)
 *
 * Do NOT target Vec3i, worst mistake of my LIFE.
 */
@Mixin(BlockPos.class)
public abstract class BlockPosMixin extends Vec3i {

    protected BlockPosMixin(int x, int y, int z) {
        super(x, y, z);
        throw new AssertionError();
    }

    @Override
    public int hashCode() {
        long hash = 3241L;
        hash = 3457689L * hash + getX();
        hash = 8734625L * hash + getY();
        return (int) (2873465L * hash + getZ());
    }

}

package zone.rong.loliasm.common.efficienthashing.mixins;

import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Thanks to
 * @author PrototypeTrousers: for bringing attention to this issue: https://i.imgur.com/RctqPTl.png
 * @author Baritone: for the hashCode implemntation. It beats out vanilla hashCode implementation especially when collisions occur (20%!)
 */
@Mixin(Vec3i.class)
public class Vec3iMixin {

    @Shadow @Final private int x;
    @Shadow @Final private int y;
    @Shadow @Final private int z;

    @Override
    public int hashCode() {
        long hash = 3241L;
        hash = 3457689L * hash + x;
        hash = 8734625L * hash + y;
        return (int) (2873465L * hash + z);
    }

}

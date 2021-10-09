package zone.rong.loliasm.common.modfixes.qmd.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "lach_01298.qmd.entity.EntityBeamProjectile")
public abstract class EntityBeamProjectileMixin extends Entity {

    protected EntityBeamProjectileMixin(World worldIn) {
        super(worldIn);
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        double length = this.getEntityBoundingBox().getAverageEdgeLength();
        if (Double.isNaN(length)) {
            length = 1.0D;
        }
        length = length * 128.0D * getRenderDistanceWeight();
        return distance < (length * length);
    }

}

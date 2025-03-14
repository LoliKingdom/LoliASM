package zone.rong.loliasm.common.modfixes.railcraft.mixins;

import com.google.common.collect.ForwardingMap;
import mods.railcraft.api.charge.IChargeBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.EnumSet;

@Pseudo
@Mixin(targets = "mods.railcraft.common.util.charge.ChargeNetwork$ConnectionMap", remap = false)
public abstract class ConnectionMapMixin extends ForwardingMap<Vec3i, EnumSet<IChargeBlock.ConnectType>> {

    @Override
    public EnumSet<IChargeBlock.ConnectType> put(Vec3i key, EnumSet<IChargeBlock.ConnectType> value) {
        if (key.getClass() == Vec3i.class) {
            key = new BlockPos(key.getX(), key.getY(), key.getZ());
        }
        return super.put(key, value);
    }

}

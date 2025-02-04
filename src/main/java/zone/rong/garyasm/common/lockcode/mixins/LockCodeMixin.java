package zone.rong.garyasm.common.lockcode.mixins;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.LockCode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Fixes LockCode not using its EMPTY variant when loading the EMPTY one from nbt
 */
@Mixin(LockCode.class)
public class LockCodeMixin {

    @Shadow @Final public static LockCode EMPTY_CODE;

    /**
     * @author Rongmario
     * @reason Use EMPTY_CODE if Lock string isEmpty, which happens every time a tile that uses this is loaded from nbt
     */
    @Overwrite
    public static LockCode fromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("Lock", 8)) {
            String s = nbt.getString("Lock");
            return s.isEmpty() ? EMPTY_CODE : new LockCode(s);
        } else {
            return EMPTY_CODE;
        }
    }

}

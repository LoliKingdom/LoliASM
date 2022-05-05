package zone.rong.loliasm.common.crashes.mixins;

import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntity.class)
public class TileEntityMixin {

    // "Block Entity" to stay consistent with vanilla strings
    @Inject(method = "addInfoToCrashReport", at = @At("TAIL"))
    private void onAddEntityCrashInfo(CrashReportCategory category, CallbackInfo ci) {
        category.addDetail("Block Entity NBT", () -> ((TileEntity) (Object) this).writeToNBT(new NBTTagCompound()).toString());
    }

}

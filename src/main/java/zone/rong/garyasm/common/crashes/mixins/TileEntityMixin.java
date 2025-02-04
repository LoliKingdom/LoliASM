package zone.rong.garyasm.common.crashes.mixins;

import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.garyasm.common.crashes.CrashUtils;

@Mixin(TileEntity.class)
public class TileEntityMixin {

    @Inject(method = "addInfoToCrashReport", at = @At("TAIL"))
    private void onAddEntityCrashInfo(CrashReportCategory category, CallbackInfo ci) {
        if (!CrashUtils.WRITING_DETAIL.get()) {
            CrashUtils.WRITING_DETAIL.set(true);
            category.addDetail("NBT", () -> ((TileEntity) (Object) this).writeToNBT(new NBTTagCompound()).toString());
            CrashUtils.WRITING_DETAIL.set(false);
        }
    }

}

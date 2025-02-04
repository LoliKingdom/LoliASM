package zone.rong.loliasm.common.crashes.mixins;

import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.common.crashes.CrashUtils;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "addEntityCrashInfo", at = @At("TAIL"))
    private void onAddEntityCrashInfo(CrashReportCategory category, CallbackInfo ci) {
        if (!CrashUtils.WRITING_DETAIL.get()) {
            CrashUtils.WRITING_DETAIL.set(true);
            category.addDetail("Entity NBT", () -> ((Entity) (Object) this).writeToNBT(new NBTTagCompound()).toString());
            CrashUtils.WRITING_DETAIL.set(false);
        }
    }

}

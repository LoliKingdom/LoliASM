package zone.rong.loliasm.common.crashes.mixins;

import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "addEntityCrashInfo", at = @At("TAIL"))
    private void onAddEntityCrashInfo(CrashReportCategory category, CallbackInfo ci) {
        category.addDetail("Entity NBT", () -> ((Entity) (Object) this).writeToNBT(new NBTTagCompound()).toString());
    }

}

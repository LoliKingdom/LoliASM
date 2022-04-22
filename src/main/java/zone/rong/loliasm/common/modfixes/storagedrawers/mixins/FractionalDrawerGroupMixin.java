package zone.rong.loliasm.common.modfixes.storagedrawers.mixins;

import com.jaquadro.minecraft.storagedrawers.api.storage.IDrawerAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.FractionalDrawerGroup$FractionalStorage", remap = false)
public class FractionalDrawerGroupMixin {
    @Shadow
    IDrawerAttributes attrs;

    @Inject(method = {"adjustStoredItemCount"}, at = @At(value = "HEAD"), cancellable = true)
    public void adjustStoredItemCount(int slot, int amount, CallbackInfoReturnable<Integer> cir) {
        if (this.attrs.isUnlimitedVending()) {
            cir.setReturnValue(0);
        }
    }

}

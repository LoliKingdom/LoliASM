package zone.rong.loliasm.common.modfixes.storagedrawers.mixins;

import com.jaquadro.minecraft.storagedrawers.capabilities.DrawerItemHandler;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

@Mixin(value = DrawerItemHandler.class, remap = false)
public class DrawerItemHandlerMixin {
    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
    public void insertItem(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        if (slot == 0) {
            cir.setReturnValue(stack);
        }
        cir.setReturnValue(this.insertItemInternal(slot, stack, simulate));
    }

    @Shadow
    private ItemStack insertItemInternal(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return null;
    }

}

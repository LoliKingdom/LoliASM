package zone.rong.loliasm.common.modfixes.storagedrawers.mixins;

import com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityController;
import com.jaquadro.minecraft.storagedrawers.block.tile.tiledata.ControllerData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ControllerData.class, remap = false)
public class ControllerDataMixin {
    @Shadow
    private BlockPos controllerCoord;

    private TileEntityController controller;

    @Inject(method = "getController", at = @At(value = "HEAD"), cancellable = true)
    void getController(TileEntity host, CallbackInfoReturnable<TileEntityController> cir) {
        if (controller != null) {
            if (!controller.isInvalid()) {
                cir.setReturnValue(controller);
            }
            controller = null;
            host.markDirty();
        }
        if (controllerCoord != null) {
            controller = (TileEntityController) host.getWorld().getTileEntity(controllerCoord);
        }
        cir.setReturnValue(controller);
    }

    @Inject(method = "bindCoord", at = @At(value = "RETURN"))
    void bindCoord(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        controller = null;
    }

}

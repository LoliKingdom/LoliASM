package zone.rong.loliasm.common.crashes.mixins;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.common.crashes.IStateful;

@Mixin(TileEntityRendererDispatcher.class)
public class TileEntityRenderDispatcherMixin implements IStateful {

    @Shadow private boolean drawingBatch;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onInit(CallbackInfo ci) {
        register();
    }

    @Override
    public void reset() {
        if (drawingBatch) {
            drawingBatch = false;
        }
    }
}

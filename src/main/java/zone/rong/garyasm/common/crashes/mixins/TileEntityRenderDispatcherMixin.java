package zone.rong.garyasm.common.crashes.mixins;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.garyasm.common.crashes.IStateful;

@Mixin(TileEntityRendererDispatcher.class)
public class TileEntityRenderDispatcherMixin implements IStateful {

    @Shadow private boolean drawingBatch;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onInit(CallbackInfo ci) {
        register();
    }

    @Override
    public void resetState() {
        if (drawingBatch) {
            drawingBatch = false;
        }
    }
}

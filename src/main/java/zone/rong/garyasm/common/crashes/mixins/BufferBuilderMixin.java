package zone.rong.garyasm.common.crashes.mixins;

import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.garyasm.common.crashes.IStateful;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements IStateful {

    @Shadow public boolean isDrawing;

    @Shadow public abstract void finishDrawing();

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onInit(int bufferSizeIn, CallbackInfo ci) {
        register();
    }

    @Override
    public void resetState() {
        if (isDrawing) {
            finishDrawing();
        }
    }

}

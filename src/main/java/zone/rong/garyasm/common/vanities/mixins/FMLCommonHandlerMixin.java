package zone.rong.garyasm.common.vanities.mixins;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import zone.rong.garyasm.core.GaryLoadingPlugin;

@Mixin(FMLCommonHandler.class)
public class FMLCommonHandlerMixin {

    @Inject(method = "computeBranding", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList$Builder;add(Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList$Builder;", ordinal = 2, remap = false), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
    private void injectGaryASMBranding(CallbackInfo ci, ImmutableList.Builder<String> builder) {
        builder.add("GaryASM " + GaryLoadingPlugin.VERSION);
    }

}

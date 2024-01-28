package zone.rong.blahajasm.client.sprite.ondemand.mixins;

import net.minecraft.client.renderer.color.BlockColors;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.blahajasm.client.sprite.ondemand.IVertexLighterExpander;

@Mixin(value = ForgeBlockModelRenderer.class, remap = false)
public class ForgeBlockModelRendererMixin {

    @Shadow @Final @Mutable private ThreadLocal<VertexLighterFlat> lighterFlat;
    @Shadow @Final @Mutable private ThreadLocal<VertexLighterSmoothAo> lighterSmooth;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void afterInit(BlockColors colours, CallbackInfo ci) {
        this.lighterFlat = ThreadLocal.withInitial(() -> ((IVertexLighterExpander<VertexLighterFlat>) new VertexLighterFlat(colours)).primeForDispatch());
        this.lighterSmooth = ThreadLocal.withInitial(() -> ((IVertexLighterExpander<VertexLighterSmoothAo>) new VertexLighterSmoothAo(colours)).primeForDispatch());
    }

}

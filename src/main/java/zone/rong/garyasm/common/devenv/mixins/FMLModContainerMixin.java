package zone.rong.garyasm.common.devenv.mixins;

import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FMLModContainer.class, remap = false)
public class FMLModContainerMixin {

    @Shadow private ModMetadata modMetadata;

    @Inject(method = "bindMetadata", at = @At("RETURN"))
    private void voidForgeRequirements(MetadataCollection mc, CallbackInfo ci) {
        if (modMetadata.requiredMods != null) {
            modMetadata.requiredMods.removeIf(av -> av.getLabel().equals("forge"));
        }
        if (modMetadata.dependencies != null) {
            modMetadata.dependencies.removeIf(av -> av.getLabel().equals("forge"));
        }
    }

}

package zone.rong.blahajasm.common.modfixes.b3m.mixins;

import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import sedridor.B3M.B3M_Core;
import zone.rong.blahajasm.common.modfixes.b3m.CapitalizedNamespaceResourceLocation;

@Mixin(value = B3M_Core.class, remap = false)
public class B3M_CoreMixin {

    @Shadow @Final @Mutable private static final ResourceLocation newMoonPhasesPng = new CapitalizedNamespaceResourceLocation("B3M", "textures/moon_phases.png");
    @Shadow @Final @Mutable private static final ResourceLocation newSunPng = new CapitalizedNamespaceResourceLocation("B3M", "textures/sun.png");
    @Shadow @Final @Mutable private static final ResourceLocation newMoonPhases2Png = new CapitalizedNamespaceResourceLocation("B3M", "textures/moon_phases2.png");
    @Shadow @Final @Mutable private static final ResourceLocation newSun2Png = new CapitalizedNamespaceResourceLocation("B3M", "textures/sun2.png");
    @Shadow @Final @Mutable private static final ResourceLocation newMoonPhasesBlank = new CapitalizedNamespaceResourceLocation("B3M", "textures/moon_phases_blank.png");
    @Shadow @Final @Mutable private static final ResourceLocation newSunBlank = new CapitalizedNamespaceResourceLocation("B3M", "textures/sun_blank.png");
    @Shadow @Final @Mutable private static final ResourceLocation[] newClouds = new CapitalizedNamespaceResourceLocation[] {
            new CapitalizedNamespaceResourceLocation("B3M", "textures/clouds_0.png"),
            new CapitalizedNamespaceResourceLocation("B3M", "textures/clouds_1.png"),
            new CapitalizedNamespaceResourceLocation("B3M", "textures/clouds_2.png"),
            new CapitalizedNamespaceResourceLocation("B3M", "textures/clouds_3.png"),
            new CapitalizedNamespaceResourceLocation("B3M", "textures/clouds_4.png"),
            new CapitalizedNamespaceResourceLocation("B3M", "textures/clouds_5.png"),
            new CapitalizedNamespaceResourceLocation("B3M", "textures/clouds_6.png"),
            new CapitalizedNamespaceResourceLocation("B3M", "textures/clouds_7.png"),
            new CapitalizedNamespaceResourceLocation("B3M", "textures/clouds_8.png"),
            new CapitalizedNamespaceResourceLocation("B3M", "textures/clouds_9.png")
    };

}

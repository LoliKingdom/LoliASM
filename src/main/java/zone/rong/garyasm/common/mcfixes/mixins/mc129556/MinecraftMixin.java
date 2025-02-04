package zone.rong.garyasm.common.mcfixes.mixins.mc129556;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow @Final public Profiler profiler;

    /** @reason Fix GUI logic being included as part of "root.tick.textures" (https://bugs.mojang.com/browse/MC-129556) */
    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", ordinal = 0))
    private void endStartGUISection(Profiler profiler, String name) {
        profiler.endStartSection("gui");
    }

    /** @reason Part 2 of GUI logic fix. */
    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;tick()V", ordinal = 0))
    private void tickTextureManagerWithCorrectProfiler(TextureManager textureManager) {
        profiler.endStartSection("textures");
        textureManager.tick();
        profiler.endStartSection("gui");
    }

}

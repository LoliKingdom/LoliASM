package zone.rong.loliasm.vanillafix.bugfixes.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.profiler.ISnooperInfo;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.IThreadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IThreadListener, ISnooperInfo {

    @Shadow @Final public Profiler profiler;

    @Shadow public GuiIngame ingameGUI;

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

    /** @reason Removes a call to {@link System#gc()} to make world loading as fast as possible */
    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Ljava/lang/System;gc()V"), cancellable = true)
    private void onSystemGC(WorldClient worldClient, String reason, CallbackInfo ci) {
        ci.cancel();
    }
}

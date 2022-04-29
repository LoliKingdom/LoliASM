package zone.rong.loliasm.vanillafix.bugfixes.mixins.client;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EntityRenderer.class, priority = 1500)
public class MixinEntityRenderer {
    /** @reason Makes the third-person view camera pass through non-solid blocks (fixes https://bugs.mojang.com/browse/MC-30845) */
    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;"), expect = 0)
    private RayTraceResult rayTraceBlocks(WorldClient world, Vec3d from, Vec3d to) {
        return world.rayTraceBlocks(from, to, false, true, true);
    }

    /** @reason Removes the sky dimming effect seen before 1.13 (fixes https://bugs.mojang.com/browse/MC-31681) */
    @Redirect(method = "updateRenderer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;renderDistanceChunks:I", ordinal = 0))
    private int getFakeRenderDistance(GameSettings settings) {
        return 32;
    }
}

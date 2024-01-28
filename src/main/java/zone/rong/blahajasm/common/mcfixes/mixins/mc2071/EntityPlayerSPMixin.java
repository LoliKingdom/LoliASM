package zone.rong.blahajasm.common.mcfixes.mixins.mc2071;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// TODO: ditch mixin
@Mixin(EntityPlayerSP.class)
public class EntityPlayerSPMixin {

    /**
     * @reason Enables opening GUIs in nether portals. (see https://bugs.mojang.com/browse/MC-2071). This works by making minecraft think that GUI pauses the game.
     */
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;doesGuiPauseGame()Z"))
    private boolean onPauseCheck(GuiScreen guiScreen) {
        return true;
    }

}

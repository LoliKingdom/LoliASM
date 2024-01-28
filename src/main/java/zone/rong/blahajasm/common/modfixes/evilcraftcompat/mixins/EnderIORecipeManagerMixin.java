package zone.rong.blahajasm.common.modfixes.evilcraftcompat.mixins;

import crazypants.enderio.base.recipe.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "org.cyclops.evilcraftcompat.modcompat.enderio.EnderIORecipeManager", remap = false)
public class EnderIORecipeManagerMixin {

    @SuppressWarnings("all")
    @Redirect(method = "register", at = @At(value = "NEW", target = "crazypants/enderio/base/recipe/Recipe"))
    private static Recipe resolveCorrectRecipeSignature(IRecipeInput recipeInput, int energyRequired, RecipeBonusType bonusType, RecipeOutput... output) {
        return new Recipe(recipeInput, energyRequired, bonusType, RecipeLevel.IGNORE, output);
    }

}

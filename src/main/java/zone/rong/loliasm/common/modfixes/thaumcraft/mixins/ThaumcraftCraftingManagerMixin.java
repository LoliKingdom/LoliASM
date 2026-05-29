package zone.rong.loliasm.common.modfixes.thaumcraft.mixins;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;
import zone.rong.loliasm.common.modfixes.thaumcraft.ThaumcraftRecipeIndex;

@Mixin(value = ThaumcraftCraftingManager.class, remap = false)
public abstract class ThaumcraftCraftingManagerMixin {

    @Shadow
    private static AspectList getAspectsFromIngredients(NonNullList<net.minecraft.item.crafting.Ingredient> ingredients,
                                                        ItemStack recipeOut, IRecipe recipe, ArrayList<String> history) {
        throw new AssertionError();
    }

    /**
     * @author obus-globus
     * @reason Optimization
     */
    @Overwrite
    private static AspectList generateTagsFromCraftingRecipes(ItemStack stack, ArrayList<String> history) {
        List<IRecipe> candidates = ThaumcraftRecipeIndex.forItem(stack.getItem());
        if (candidates.isEmpty()) {
            return null;
        }

        int idS = stack.getMetadata() == 32767 ? 0 : stack.getMetadata();
        AspectList ret = null;
        int value = Integer.MAX_VALUE;

        for (int i = 0, n = candidates.size(); i < n; i++) {
            IRecipe recipe = candidates.get(i);
            ItemStack out = recipe.getRecipeOutput();
            // preserve the original's per-recipe null/empty guard
            if (out == null || out.isEmpty()) {
                continue;
            }
            int idR = out.getMetadata() == 32767 ? 0 : out.getMetadata();
            if (idR != idS) {
                continue;
            }
            try {
                AspectList ph = getAspectsFromIngredients(recipe.getIngredients(), out, recipe, history);
                if (recipe instanceof IArcaneRecipe) {
                    IArcaneRecipe ar = (IArcaneRecipe) recipe;
                    if (ar.getVis() > 0) {
                        ph.add(Aspect.MAGIC, (int) (Math.sqrt(1 + (ar.getVis() / 2)) / out.getCount()));
                    }
                }
                for (Aspect as : ph.copy().getAspects()) {
                    if (ph.getAmount(as) <= 0) {
                        ph.remove(as);
                    }
                }
                if (ph.visSize() < value && ph.visSize() > 0) {
                    ret = ph;
                    value = ph.visSize();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
}

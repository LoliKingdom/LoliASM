package zone.rong.loliasm.common.recipes.mixins;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.fml.common.FMLLog;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

/**
 * FurnaceRecipes are only item/meta sensitive, hence a simple Strategy is used.
 *
 * getSmeltingResult is now only a hash lookup.
 * experienceList doesn't have its floats boxed now.
 */
@Mixin(FurnaceRecipes.class)
public abstract class FurnaceRecipesMixin {

    @Shadow @Final private Map<ItemStack, ItemStack> smeltingList;
    @Shadow @Final private Map<ItemStack, Float> experienceList;

    /**
     * @author Rongmario
     * @reason Use Object2FloatMap#put
     */
    @Overwrite
    public void addSmeltingRecipe(ItemStack input, ItemStack stack, float experience) {
        if (getSmeltingResult(input) != ItemStack.EMPTY) {
            FMLLog.log.info("Ignored smelting recipe with conflicting input: {} = {}", input, stack); return;
        }
        this.smeltingList.put(input, stack);
        ((Object2FloatMap<ItemStack>) this.experienceList).put(stack, experience);
    }

    /**
     * @author Rongmario
     * @reason Efficient getSmeltingResult
     */
    @Overwrite
    public ItemStack getSmeltingResult(ItemStack stack) {
        ItemStack result = this.smeltingList.get(stack);
        return result == null ? ItemStack.EMPTY : result;
    }

    /**
     * @author Rongmario
     * @reason Efficient getSmeltingExperience
     */
    @Overwrite
    public float getSmeltingExperience(ItemStack stack) {
        float exp = stack.getItem().getSmeltingExperience(stack);
        if (exp == -1) {
            exp = ((Object2FloatMap<ItemStack>) this.experienceList).getFloat(stack);
        }
        return exp;
    }

}

package zone.rong.garyasm.common.recipes.mixins;

import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.fml.common.FMLLog;
import org.spongepowered.asm.mixin.*;
import zone.rong.garyasm.config.GaryConfig;

import java.util.Map;

/**
 * FurnaceRecipes are only item/meta sensitive, hence a simple Strategy is used.
 *
 * getSmeltingResult/getSmeltingExperience is now only a hash lookup.
 * experienceList doesn't have its floats boxed now.
 */
@Mixin(FurnaceRecipes.class)
public abstract class FurnaceRecipesMixin {

    @Shadow @Final private Map<ItemStack, ItemStack> smeltingList;
    @Shadow @Final private Map<ItemStack, Float> experienceList;

    @Shadow protected abstract boolean compareItemStacks(ItemStack stack1, ItemStack stack2);

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
        if (GaryConfig.instance.furnaceExperienceMost) {
            float prevExperience = ((Object2FloatMap<ItemStack>) experienceList).getFloat(stack);
            if (experience > prevExperience) {
                ((Object2FloatMap<ItemStack>) experienceList).put(stack, experience);
            }
        } else if (GaryConfig.instance.furnaceExperienceVanilla) {
            this.experienceList.put(stack, experience);
        } else {
            this.experienceList.putIfAbsent(stack, experience);
        }
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
            if (GaryConfig.instance.furnaceExperienceVanilla) {
                for (Map.Entry<ItemStack, Float> entry : this.experienceList.entrySet()) {
                    if (this.compareItemStacks(stack, entry.getKey())) {
                        return entry.getValue();
                    }
                }
            } else {
                return ((Object2FloatMap<ItemStack>) this.experienceList).getFloat(stack);
            }
        }
        return exp;
    }

}

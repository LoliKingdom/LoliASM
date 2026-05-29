package zone.rong.loliasm.common.modfixes.thaumcraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;

// output Item -> recipes index backing ThaumcraftCraftingManagerMixin. Rebuilt whenever the recipe
// registry changes size, so a lookup always reflects the registry as it is at call time (matching
// the original full scan), regardless of when the first call happens or later recipe additions.
public final class ThaumcraftRecipeIndex {

    private static volatile Map<Item, List<IRecipe>> index;
    private static volatile int builtAtSize = -1;

    private ThaumcraftRecipeIndex() {}

    public static List<IRecipe> forItem(Item item) {
        int size = CraftingManager.REGISTRY.getKeys().size();
        Map<Item, List<IRecipe>> idx = index;
        if (idx == null || size != builtAtSize) {
            synchronized (ThaumcraftRecipeIndex.class) {
                if (index == null || size != builtAtSize) {
                    build();
                }
                idx = index;
            }
        }
        List<IRecipe> out = idx.get(item);
        return out != null ? out : Collections.emptyList();
    }

    private static void build() {
        Map<Item, List<IRecipe>> idx = new IdentityHashMap<>(4096);
        for (IRecipe recipe : CraftingManager.REGISTRY) {
            if (recipe == null) {
                continue;
            }
            ItemStack output = recipe.getRecipeOutput();
            if (output == null || output.isEmpty()) {
                continue;
            }
            Item item = output.getItem();
            if (item == null || Item.getIdFromItem(item) <= 0) {
                continue;
            }
            idx.computeIfAbsent(item, k -> new ArrayList<>(2)).add(recipe);
        }
        index = idx;
        builtAtSize = CraftingManager.REGISTRY.getKeys().size();
    }
}

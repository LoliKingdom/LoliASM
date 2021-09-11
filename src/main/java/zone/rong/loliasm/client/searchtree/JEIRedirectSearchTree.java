package zone.rong.loliasm.client.searchtree;

import mezz.jei.Internal;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientFilter;
import net.minecraft.client.util.SearchTree;
import net.minecraft.item.ItemStack;
import zone.rong.loliasm.LoliReflector;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JEIRedirectSearchTree extends SearchTree<ItemStack> {

    private static final MethodHandle handle_getIngredientListUncached = LoliReflector.resolveMethod(IngredientFilter.class, "getIngredientListUncached", String.class);

    @SuppressWarnings("ConstantConditions")
    public JEIRedirectSearchTree() {
        super(null, null);
        this.byName = null;
        this.byId = null;
        // TODO: make `List<T> contents = Lists.<T>newArrayList();` `numericContents = new Object2IntOpenHashMap<T>();` protected and set to null
    }

    @Override
    public void recalculate() { }

    @Override
    public void add(ItemStack element) { }

    @Override
    @SuppressWarnings("unchecked")
    public List<ItemStack> search(String searchText) {
        try {
            final List<ItemStack> results = new ArrayList<>();
            for (final IIngredientListElement<?> element : ((List<IIngredientListElement<?>>) handle_getIngredientListUncached.invokeExact(Internal.getIngredientFilter(), searchText))) {
                if (element.getIngredient() instanceof ItemStack) {
                    results.add((ItemStack) element.getIngredient());
                }
            }
            return results;
        } catch (Throwable t) {
            t.printStackTrace();
            return Collections.emptyList();
        }
    }
}

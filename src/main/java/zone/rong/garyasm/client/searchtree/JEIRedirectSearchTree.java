package zone.rong.loliasm.client.searchtree;

import mezz.jei.Internal;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.client.util.SearchTree;
import net.minecraft.item.ItemStack;
import zone.rong.loliasm.client.searchtree.mixins.mod.IngredientFilterInvoker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JEIRedirectSearchTree extends SearchTree<ItemStack> {

    private String lastSearch = "";
    private final List<ItemStack> resultsCache = new ArrayList<>();

    @SuppressWarnings("ConstantConditions")
    public JEIRedirectSearchTree() {
        super(null, null);
        this.byName = null;
        this.byId = null;
    }

    @Override
    public void recalculate() { }

    @Override
    public void add(ItemStack element) { }

    @Override
    @SuppressWarnings("unchecked")
    public List<ItemStack> search(String searchText) {
        if (lastSearch.equals(searchText)) {
            return resultsCache;
        }
        try {
            resultsCache.clear();
            for (IIngredientListElement element : ((IngredientFilterInvoker) Internal.getIngredientFilter()).invokeGetIngredientListUncached(searchText)) {
                if (element.getIngredient() instanceof ItemStack) {
                    resultsCache.add((ItemStack) element.getIngredient());
                }
            }
            lastSearch = searchText;
            return resultsCache;
        } catch (Throwable t) {
            return Collections.emptyList();
        }
    }

}

package mezz.jei.ingredients;

import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.suffixtree.GeneralizedSuffixTree;

import java.util.Collection;

public class PublicizedPrefixSearchTree {

    public static Collection<String> retrieveStrings(Object prefixedSearchTree, IIngredientListElement<?> element) {
        return ((PrefixedSearchTree) prefixedSearchTree).getStringsGetter().getStrings(element);
    }

    public static GeneralizedSuffixTree getTree(Object prefixedSearchTree) {
        return ((PrefixedSearchTree) prefixedSearchTree).getTree();
    }

    public static Config.SearchMode getMode(Object prefixedSearchTree) {
        return ((PrefixedSearchTree) prefixedSearchTree).getMode();
    }

}

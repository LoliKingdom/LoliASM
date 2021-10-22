package mezz.jei.ingredients;

import mezz.jei.gui.ingredients.IIngredientListElement;
import zone.rong.loliasm.LoliReflector;

import java.util.Collection;

public class PublicizedPrefixSearchTree extends PrefixedSearchTree {

    public PublicizedPrefixSearchTree(Object prefixedSearchTree) throws IllegalAccessException {
        super(((PrefixedSearchTree) prefixedSearchTree).getTree(),
                ((PrefixedSearchTree) prefixedSearchTree).getStringsGetter(),
                (IModeGetter) LoliReflector.getField(PrefixedSearchTree.class, "modeGetter").get(prefixedSearchTree));
    }

    public Collection<String> retrieveStrings(IIngredientListElement<?> element) {
        return getStringsGetter().getStrings(element);
    }

}

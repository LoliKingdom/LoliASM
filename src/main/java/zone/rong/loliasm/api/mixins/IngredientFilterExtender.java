package zone.rong.loliasm.api.mixins;

import mezz.jei.suffixtree.GeneralizedSuffixTree;

public interface IngredientFilterExtender {

    GeneralizedSuffixTree getTree(char key);

}

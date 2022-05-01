package zone.rong.loliasm.client.searchtree.mixins.mod;

import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(value = IngredientFilter.class, remap = false)
public interface IngredientFilterInvoker {

    @Invoker
    List<IIngredientListElement> invokeGetIngredientListUncached(String filterText);

}

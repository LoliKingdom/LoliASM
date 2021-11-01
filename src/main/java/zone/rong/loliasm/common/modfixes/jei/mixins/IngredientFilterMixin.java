package zone.rong.loliasm.common.modfixes.jei.mixins;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import mezz.jei.Internal;
import mezz.jei.JustEnoughItems;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.PublicizedPrefixSearchTree;
import mezz.jei.suffixtree.GeneralizedSuffixTree;
import mezz.jei.util.Translator;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.api.LoliStringPool;
import zone.rong.loliasm.api.mixins.GeneralizedSuffixTreeExtender;
import zone.rong.loliasm.api.mixins.IngredientFilterExtender;
import zone.rong.loliasm.core.LoliHooks;

import javax.annotation.Nullable;
import java.util.Collection;

@Mixin(value = IngredientFilter.class, remap = false)
public abstract class IngredientFilterMixin implements IngredientFilterExtender {

    @Shadow @Final private GeneralizedSuffixTree searchTree;
    @Shadow @Final private NonNullList<IIngredientListElement> elementList;

    @Shadow @Nullable private String filterCached;

    @Shadow public abstract <V> void updateHiddenState(IIngredientListElement<V> element);

    @Shadow @Final private Char2ObjectMap prefixedSearchTrees;

    @Unique private boolean needsAdding = false;

    @Override
    public GeneralizedSuffixTree getTree(char key) {
        return key == ' ' ? searchTree : PublicizedPrefixSearchTree.getTree(prefixedSearchTrees.get(key));
    }

    @Override
    public boolean isEnabled(char key) {
        return key == ' ' || PublicizedPrefixSearchTree.getMode(prefixedSearchTrees.get(key)) != Config.SearchMode.DISABLED;
    }

    @Inject(method = "addIngredients", at = @At("HEAD"))
    private void onAddIngredients(NonNullList<IIngredientListElement> ingredients, CallbackInfo ci) {
        LoliStringPool.establishPool(LoliStringPool.JEI_ID, 2880);
        needsAdding = Internal.getRuntime() == null || hasStarted();
    }

    /**
     * @author Rongmario
     * @reason I didn't wanna do this.
     */
    @Overwrite
    public <V> void addIngredient(IIngredientListElement<V> element) {
        updateHiddenState(element);
        final int index = elementList.size();
        elementList.add(element);
        if (needsAdding && !((GeneralizedSuffixTreeExtender) this.searchTree).isDeserialized()) {
            searchTree.put(Translator.toLowercaseWithLocale(element.getDisplayName()), index);
        }
        for (Object obj : this.prefixedSearchTrees.values()) {
            GeneralizedSuffixTree tree = PublicizedPrefixSearchTree.getTree(obj);
            if (needsAdding && !((GeneralizedSuffixTreeExtender) tree).isDeserialized()) {
                Config.SearchMode searchMode = PublicizedPrefixSearchTree.getMode(obj);
                if (searchMode != Config.SearchMode.DISABLED) {
                    Collection<String> strings = PublicizedPrefixSearchTree.retrieveStrings(obj, element);
                    for (String string : strings) {
                        tree.put(string, index);
                    }
                }
            }
        }
        filterCached = null;
    }

    @Redirect(method = "createPrefixedSearchTree", at = @At(value = "NEW", ordinal = 0))
    private GeneralizedSuffixTree retrieveTree(char prefix) {
        return LoliHooks.JEI.get(prefix);
    }

    /*
    @Redirect(method = "createPrefixedSearchTree", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/chars/Char2ObjectMap;put(CLjava/lang/Object;)Ljava/lang/Object;"))
    private Object retrieveTree(Char2ObjectMap char2ObjectMap, char key, Object value) throws IllegalAccessException {
        return char2ObjectMap.put(key, new PublicizedPrefixSearchTree(value));
    }
     */

    /*
    @SuppressWarnings("all")
    @Inject(method = "createPrefixedSearchTree", at = @At("HEAD"), cancellable = true)
    private void onCreatePrefixedSearchTree(char prefix, Object modeGetter, Object stringsGetter, CallbackInfo ci) {
        PublicizedPrefixSearchTree prefixSearchTree = new PublicizedPrefixSearchTree(LoliHooks.JEI.get(prefix), stringsGetter, modeGetter);
        this.prefixedSearchTrees.put(prefix, prefixSearchTree);
        ci.cancel(); // Effectively this is an @Overwrite
    }
     */

    private boolean hasStarted() {
        return ((AccessorProxyCommonClient) JustEnoughItems.getProxy()).getJeiStarter().hasStarted();
    }

}

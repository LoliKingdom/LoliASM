package zone.rong.loliasm.common.modfixes.jei.mixins;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import mezz.jei.Internal;
import mezz.jei.config.Config;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.PublicizedPrefixSearchTree;
import mezz.jei.suffixtree.GeneralizedSuffixTree;
import mezz.jei.util.Translator;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Override
    public GeneralizedSuffixTree getTree(char key) {
        return key == ' ' ? searchTree : ((PublicizedPrefixSearchTree) prefixedSearchTrees.get(key)).getTree();
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
        if (Internal.getRuntime() == null && !((GeneralizedSuffixTreeExtender) this.searchTree).isDeserialized()) {
            searchTree.put(Translator.toLowercaseWithLocale(element.getDisplayName()), index);
        }
        for (Object obj : this.prefixedSearchTrees.values()) {
            PublicizedPrefixSearchTree prefixedSearchTree = (PublicizedPrefixSearchTree) obj;
            GeneralizedSuffixTree tree = prefixedSearchTree.getTree();
            if (Internal.getRuntime() == null && ((GeneralizedSuffixTreeExtender) tree).isDeserialized()) {
                continue;
            }
            Config.SearchMode searchMode = prefixedSearchTree.getMode();
            if (searchMode != Config.SearchMode.DISABLED) {
                Collection<String> strings = prefixedSearchTree.retrieveStrings(element);
                for (String string : strings) {
                    tree.put(string, index);
                }
            }
        }
        filterCached = null;
    }

    @Redirect(method = "createPrefixedSearchTree", at = @At(value = "NEW", ordinal = 0))
    private GeneralizedSuffixTree retrieveTree(char prefix) {
        return LoliHooks.JEI.get(prefix);
    }

    @Redirect(method = "createPrefixedSearchTree", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/chars/Char2ObjectMap;put(CLjava/lang/Object;)Ljava/lang/Object;"))
    private Object retrieveTree(Char2ObjectMap char2ObjectMap, char key, Object value) throws IllegalAccessException {
        return char2ObjectMap.put(key, new PublicizedPrefixSearchTree(value));
    }

    /*
    @SuppressWarnings("all")
    @Inject(method = "createPrefixedSearchTree", at = @At("HEAD"), cancellable = true)
    private void onCreatePrefixedSearchTree(char prefix, Object modeGetter, Object stringsGetter, CallbackInfo ci) {
        PublicizedPrefixSearchTree prefixSearchTree = new PublicizedPrefixSearchTree(LoliHooks.JEI.get(prefix), stringsGetter, modeGetter);
        this.prefixedSearchTrees.put(prefix, prefixSearchTree);
        ci.cancel(); // Effectively this is an @Overwrite
    }
     */

}

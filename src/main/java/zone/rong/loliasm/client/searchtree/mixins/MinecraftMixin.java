package zone.rong.loliasm.client.searchtree.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.SearchTree;
import net.minecraft.client.util.SearchTreeManager;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import zone.rong.loliasm.client.searchtree.JEIRedirectSearchTree;

import java.util.Objects;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow private SearchTreeManager searchTreeManager;

    /**
     * @author Rongmario
     * @reason Use JEIRedirectSearchTree
     */
    @Overwrite
    public void populateSearchTreeManager() {
        final SearchTree<RecipeList> recipeSearchTree = new SearchTree<>(
                rl -> () -> rl.getRecipes().stream()
                        .flatMap((r) -> r.getRecipeOutput().getTooltip(null, ITooltipFlag.TooltipFlags.NORMAL).stream())
                        .map(TextFormatting::getTextWithoutFormattingCodes)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(tooltip -> !tooltip.isEmpty())
                        .iterator(),
                rl -> () -> rl.getRecipes().stream()
                        .map((r) -> r.getRecipeOutput().getItem().getRegistryName())
                        .iterator()
        );
        this.searchTreeManager.register(SearchTreeManager.RECIPES, recipeSearchTree);
        this.searchTreeManager.register(SearchTreeManager.ITEMS, new JEIRedirectSearchTree());
    }

}

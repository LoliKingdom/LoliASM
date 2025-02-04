package zone.rong.garyasm.client.searchtree.mixins.vanilla;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.client.util.SearchTree;
import net.minecraft.client.util.SearchTreeManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.garyasm.client.searchtree.JEIRedirectSearchTree;
import zone.rong.garyasm.config.GaryConfig;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow private SearchTreeManager searchTreeManager;

    @Inject(method = "populateSearchTreeManager", at = @At("HEAD"), cancellable = true)
    private void redirectToJeiSearchTree(CallbackInfo ci) {
        if (GaryConfig.instance.replaceSearchTreeWithJEISearching && Loader.isModLoaded("jei")) {
            final SearchTree<RecipeList> recipeSearchTree = new SearchTree<>(
                    rl -> () -> rl.getRecipes().stream()
                            .flatMap((r) -> r.getRecipeOutput().getTooltip(null, TooltipFlags.NORMAL).stream())
                            .map(TextFormatting::getTextWithoutFormattingCodes)
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(tooltip -> !tooltip.isEmpty())
                            .iterator(),
                    rl -> () -> rl.getRecipes().stream()
                            .map((r) -> r.getRecipeOutput().getItem().getRegistryName())
                            .iterator()
            );
            final SearchTree<ItemStack> itemSearchTree = Loader.isModLoaded("jei") ?
                    new JEIRedirectSearchTree() :
                    new SearchTree<>(stack ->
                            stack.getTooltip(null, TooltipFlags.NORMAL)
                                    .stream()
                                    .map(TextFormatting::getTextWithoutFormattingCodes)
                                    .map(String::trim)
                                    .filter(tooltip -> !tooltip.isEmpty())
                                    .collect(Collectors.toList()),
                            stack -> Collections.singleton(stack.getItem().getRegistryName()));
            this.searchTreeManager.register(SearchTreeManager.RECIPES, recipeSearchTree);
            this.searchTreeManager.register(SearchTreeManager.ITEMS, itemSearchTree);
            ci.cancel();
        }
    }

}

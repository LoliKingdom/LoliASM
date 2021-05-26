package zone.rong.loliasm.common.modfixes.astralsorcery.mixins;

import hellfirepvp.astralsorcery.common.enchantment.amulet.EnchantmentUpgradeHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EnchantmentUpgradeHelper.class)
public interface EnchantmentUpgradeHelperInvoker {

    @Invoker("removeAmuletOwner")
    static void loliasm$removeAmuletOwner(ItemStack stack) { throw new AssertionError(); }

}

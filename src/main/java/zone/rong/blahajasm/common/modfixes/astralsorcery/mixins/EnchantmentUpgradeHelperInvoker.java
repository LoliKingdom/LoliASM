package zone.rong.blahajasm.common.modfixes.astralsorcery.mixins;

import hellfirepvp.astralsorcery.common.enchantment.amulet.EnchantmentUpgradeHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EnchantmentUpgradeHelper.class)
public interface EnchantmentUpgradeHelperInvoker {

    @Invoker(value = "removeAmuletOwner", remap = false)
    static void blahajasm$removeAmuletOwner(ItemStack stack) { throw new AssertionError(); }

}

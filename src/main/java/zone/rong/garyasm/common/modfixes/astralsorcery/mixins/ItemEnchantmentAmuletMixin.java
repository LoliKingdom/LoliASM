package zone.rong.loliasm.common.modfixes.astralsorcery.mixins;

import baubles.api.IBauble;
import hellfirepvp.astralsorcery.common.enchantment.amulet.EnchantmentUpgradeHelper;
import hellfirepvp.astralsorcery.common.item.wearable.ItemEnchantmentAmulet;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemEnchantmentAmulet.class)
public abstract class ItemEnchantmentAmuletMixin implements IBauble {

    private static boolean isItemBlacklisted(ItemStack stack) {
        if (stack.isEmpty() || stack.getItemDamage() == Short.MAX_VALUE || stack.getMaxStackSize() > 1) {
            return true;
        }
        Item item = stack.getItem();
        if (!(item instanceof ItemPotion) && !(item instanceof ItemEnchantedBook)) {
            return item.getRegistryName().getNamespace().equals("draconicevolution");
        }
        return true;
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase entityLivingBase) {
        EntityPlayer player = (EntityPlayer) entityLivingBase;
        for (ItemStack armorStack : player.getArmorInventoryList()) {
            if (!isItemBlacklisted(armorStack)) {
                EnchantmentUpgradeHelper.applyAmuletOwner(armorStack, player);
            }
        }
        ItemStack handStack = player.getHeldItemMainhand();
        if (!isItemBlacklisted(handStack)) {
            EnchantmentUpgradeHelper.applyAmuletOwner(handStack, player);
        }
        handStack = player.getHeldItemOffhand();
        if (!isItemBlacklisted(handStack)) {
            EnchantmentUpgradeHelper.applyAmuletOwner(handStack, player);
        }
    }

    // Doesn't take into account on player death: see PlayerAmuletHandlerMixin
    @Override
    public void onUnequipped(ItemStack stack, EntityLivingBase player) {
        player.playSound(SoundEvents.BLOCK_GLASS_PLACE, 0.65F, 6.4F);
        for (ItemStack armorStack : player.getArmorInventoryList()) {
            EnchantmentUpgradeHelperInvoker.loliasm$removeAmuletOwner(armorStack);
        }
        EnchantmentUpgradeHelperInvoker.loliasm$removeAmuletOwner(player.getHeldItemMainhand());
        EnchantmentUpgradeHelperInvoker.loliasm$removeAmuletOwner(player.getHeldItemOffhand());
    }

}

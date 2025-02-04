package zone.rong.garyasm.common.modfixes.astralsorcery.mixins;

import hellfirepvp.astralsorcery.common.auxiliary.tick.ITickHandler;
import hellfirepvp.astralsorcery.common.enchantment.EnchantmentPlayerWornTick;
import hellfirepvp.astralsorcery.common.enchantment.amulet.PlayerAmuletHandler;
import hellfirepvp.astralsorcery.common.registry.RegistryEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerAmuletHandler.class)
public abstract class PlayerAmuletHandlerMixin implements ITickHandler {

    @SubscribeEvent
    public void onPlayerDeath(PlayerDropsEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        for (ItemStack armorStack : player.getArmorInventoryList()) {
            EnchantmentUpgradeHelperInvoker.garyasm$removeAmuletOwner(armorStack);
        }
        EnchantmentUpgradeHelperInvoker.garyasm$removeAmuletOwner(player.getHeldItemMainhand());
        EnchantmentUpgradeHelperInvoker.garyasm$removeAmuletOwner(player.getHeldItemOffhand());
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        EntityPlayer player = (EntityPlayer) context[0];
        for (EnchantmentPlayerWornTick enchant : RegistryEnchantments.wearableTickEnchantments) {
            int max = EnchantmentHelper.getMaxEnchantmentLevel(enchant, player);
            if (max > 0) {
                enchant.onWornTick(type == TickEvent.Type.CLIENT, player, max);
            }
        }
    }
}

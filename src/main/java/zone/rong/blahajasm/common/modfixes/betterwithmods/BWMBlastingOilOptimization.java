package zone.rong.blahajasm.common.modfixes.betterwithmods;

import betterwithmods.common.BWMItems;
import betterwithmods.common.items.ItemMaterial;
import betterwithmods.module.gameplay.Gameplay;
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap;
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class BWMBlastingOilOptimization {

    private static Reference2DoubleMap<EntityItem> highestPoint;

    @SubscribeEvent
    public static void onPlayerTakeDamage(LivingHurtEvent e) {
        if (e.getSource() == null || e.getSource().damageType == null || Gameplay.blacklistDamageSources.contains(e.getSource().damageType)) {
            return;
        }
        EntityLivingBase living = e.getEntityLiving();
        if (living.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler inventory = living.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (inventory != null) {
                int count = 0;
                for (int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack stack = inventory.getStackInSlot(i);
                    if (stack.getItem() == BWMItems.MATERIAL && stack.getMetadata() == ItemMaterial.EnumMaterial.BLASTING_OIL.getMetadata()) {
                        count += stack.getCount();
                        inventory.extractItem(i, stack.getCount(), false);
                    }
                }
                if (count > 0) {
                    living.attackEntityFrom(new DamageSource("blasting_oil"), Float.MAX_VALUE);
                    living.getEntityWorld().createExplosion(null, living.posX, living.posY + living.height / 16, living.posZ, (float) (Math.sqrt((double) count / 5) / 2.5 + 1), true);
                }
            }
        }
    }

    public static boolean inject$ItemMaterial$onEntityItemUpdate(EntityItem entity) {
        ItemStack stack = entity.getItem();
        if (stack.getMetadata() != ItemMaterial.EnumMaterial.BLASTING_OIL.getMetadata()) {
            return false;
        }
        if (highestPoint == null) {
            highestPoint = new Reference2DoubleOpenHashMap<>();
        }
        if (entity.isBurning() || (entity.onGround && Math.abs(entity.posY - highestPoint.getOrDefault(entity, entity.posY)) > 2.0)) {
            int count = stack.getCount();
            if (count > 0) {
                highestPoint.removeDouble(entity);
                entity.getEntityWorld().createExplosion(entity, entity.posX, entity.posY + entity.height / 16, entity.posZ, (float) (Math.sqrt((double) count / 5) / 2.5 + 1), true);
                entity.setDead();
                return true;
            }
        }
        if (entity.motionY > 0 || !highestPoint.containsKey(entity)) {
            highestPoint.put(entity, entity.posY);
        }
        return false;
    }

}

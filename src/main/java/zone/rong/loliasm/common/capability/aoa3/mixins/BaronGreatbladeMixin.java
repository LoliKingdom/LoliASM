package zone.rong.loliasm.common.capability.aoa3.mixins;

import net.minecraft.item.ItemStack;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.tslat.aoa3.item.weapon.greatblade.BaronGreatblade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BaronGreatblade.class)
public abstract class BaronGreatbladeMixin {
    @ModifyExpressionValue(method = "attackEntity", at = @At(value = "NEW", target = "(Lnet/minecraft/item/Item;)Lnet/minecraft/item/ItemStack;", remap = false))
    private ItemStack forceInitializeGrenadeTag(ItemStack original) {
        // AoA uses this method as a hack to add their nbt, and this isn't called if delayItemStackCapabilityInit is enabled
        original.getItem().initCapabilities(original, null);
        return original;
    }
}

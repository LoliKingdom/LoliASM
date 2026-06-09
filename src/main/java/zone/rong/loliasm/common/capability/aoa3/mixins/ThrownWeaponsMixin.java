package zone.rong.loliasm.common.capability.aoa3.mixins;

import net.minecraft.item.ItemStack;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.tslat.aoa3.item.weapon.thrown.BaseThrownWeapon;
import net.tslat.aoa3.item.weapon.thrown.Chakram;
import net.tslat.aoa3.item.weapon.thrown.GooBall;
import net.tslat.aoa3.item.weapon.thrown.Grenade;
import net.tslat.aoa3.item.weapon.thrown.Hellfire;
import net.tslat.aoa3.item.weapon.thrown.RunicBomb;
import net.tslat.aoa3.item.weapon.thrown.SliceStar;
import net.tslat.aoa3.item.weapon.thrown.Vulkram;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = {
        Chakram.class,
        GooBall.class,
        Grenade.class,
        Hellfire.class,
        RunicBomb.class,
        SliceStar.class,
        Vulkram.class
}, remap = false)
public abstract class ThrownWeaponsMixin extends BaseThrownWeapon {
    private ThrownWeaponsMixin(double dmg, int fireDelayTicks) {
        super(dmg, fireDelayTicks);
    }

    @ModifyExpressionValue(method = "findAndConsumeAmmo", at = @At(value = "NEW", target = "(Lnet/minecraft/item/Item;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack forceInitializeTag(ItemStack original) {
        // AoA uses this method as a hack to add their nbt, and this isn't called if delayItemStackCapabilityInit is enabled
        initCapabilities(original, null);
        return original;
    }
}

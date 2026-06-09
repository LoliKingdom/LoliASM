package zone.rong.loliasm.common.capability.aoa3.mixins;

import net.minecraft.item.ItemStack;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.tslat.aoa3.item.weapon.cannon.AncientBomber;
import net.tslat.aoa3.item.weapon.cannon.BlastCannon;
import net.tslat.aoa3.item.weapon.cannon.BombLauncher;
import net.tslat.aoa3.item.weapon.cannon.BoomBoom;
import net.tslat.aoa3.item.weapon.cannon.BoomCannon;
import net.tslat.aoa3.item.weapon.cannon.ErebonStickler;
import net.tslat.aoa3.item.weapon.cannon.FloroRPG;
import net.tslat.aoa3.item.weapon.cannon.LuxonStickler;
import net.tslat.aoa3.item.weapon.cannon.MissileMaker;
import net.tslat.aoa3.item.weapon.cannon.PlutonStickler;
import net.tslat.aoa3.item.weapon.cannon.RPG;
import net.tslat.aoa3.item.weapon.cannon.SelyanStickler;
import net.tslat.aoa3.item.weapon.gun.BaseGun;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = {
        AncientBomber.class,
        BlastCannon.class,
        BombLauncher.class,
        BoomBoom.class,
        BoomCannon.class,
        ErebonStickler.class,
        FloroRPG.class,
        LuxonStickler.class,
        MissileMaker.class,
        PlutonStickler.class,
        RPG.class,
        SelyanStickler.class,
}, remap = false)
public abstract class CannonsMixin extends BaseGun {
    private CannonsMixin(double dmg, int durability, int fireDelayTicks, float recoil) {
        super(dmg, durability, fireDelayTicks, recoil);
    }

    @ModifyExpressionValue(method = "findAndConsumeAmmo", at = @At(value = "NEW", target = "(Lnet/minecraft/item/Item;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack forceInitializeTag(ItemStack original) {
        // AoA uses this method as a hack to add their nbt, and this isn't called if delayItemStackCapabilityInit is enabled
        initCapabilities(original, null);
        return original;
    }
}

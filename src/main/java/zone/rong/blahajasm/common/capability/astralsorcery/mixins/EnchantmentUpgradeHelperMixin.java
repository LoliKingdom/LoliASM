package zone.rong.blahajasm.common.capability.astralsorcery.mixins;

import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletHolderCapability;
import hellfirepvp.astralsorcery.common.enchantment.amulet.EnchantmentUpgradeHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import zone.rong.blahajasm.api.IItemStackCapabilityInjector;
import zone.rong.blahajasm.api.ICapabilityDispatcherManipulator;

@Mixin(value = EnchantmentUpgradeHelper.class, remap = false)
public class EnchantmentUpgradeHelperMixin {

    /**
     * @author Rongmario
     * @reason Only apply AmuletHolderCapability when needed.
     */
    @Overwrite
    public static void applyAmuletOwner(ItemStack tool, EntityPlayer wearer) {
        AmuletHolderCapability cap = tool.getCapability(AmuletHolderCapability.CAPABILITY_AMULET_HOLDER, null);
        if (cap == null) {
            AmuletHolderCapability.Provider provider = new AmuletHolderCapability.Provider();
            ((IItemStackCapabilityInjector) (Object) tool).initDispatcher(AmuletHolderCapability.CAP_AMULETHOLDER_NAME, provider);
            ((AmuletHolderCapabilityProviderAccessor) provider).blahajasm$defaultImplFastAccess().setHolderUUID(wearer.getUniqueID());
        } else {
            cap.setHolderUUID(wearer.getUniqueID());
        }
    }

    /**
     * @author Rongmario
     * @reason Remove capability when the stack no longer needs it
     */
    @Overwrite
    private static void removeAmuletOwner(ItemStack tool) {
        AmuletHolderCapability cap = tool.getCapability(AmuletHolderCapability.CAPABILITY_AMULET_HOLDER, null);
        if (cap != null) {
            ((ICapabilityDispatcherManipulator) (Object) ((IItemStackCapabilityInjector) (Object) tool).getDispatcher()).stripCapability(AmuletHolderCapability.CAP_AMULETHOLDER_NAME, AmuletHolderCapability.CAPABILITY_AMULET_HOLDER, null, cap);
        }
    }

}

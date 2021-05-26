package zone.rong.loliasm.common.capability.astralsorcery.mixins;

import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletHolderCapability;
import hellfirepvp.astralsorcery.common.enchantment.amulet.EnchantmentUpgradeHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import zone.rong.loliasm.api.ICapabilityDispatcherResolver;
import zone.rong.loliasm.api.IDelayCapabilityDispatcher;

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
            ((ICapabilityDispatcherResolver) (Object) tool).initDispatcher(AmuletHolderCapability.CAP_AMULETHOLDER_NAME, provider);
            ((AmuletHolderCapabilityProviderAccessor) provider).loliasm$defaultImplFastAccess().setHolderUUID(wearer.getUniqueID());
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
            ((IDelayCapabilityDispatcher) (Object) ((ICapabilityDispatcherResolver) (Object) tool).getDispatcher()).stripCapability(AmuletHolderCapability.CAP_AMULETHOLDER_NAME, AmuletHolderCapability.CAPABILITY_AMULET_HOLDER, null, cap);
        }
    }

}

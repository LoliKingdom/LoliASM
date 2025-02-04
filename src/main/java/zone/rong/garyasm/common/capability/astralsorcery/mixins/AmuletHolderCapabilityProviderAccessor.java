package zone.rong.loliasm.common.capability.astralsorcery.mixins;

import hellfirepvp.astralsorcery.common.enchantment.amulet.AmuletHolderCapability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AmuletHolderCapability.Provider.class, remap = false)
public interface AmuletHolderCapabilityProviderAccessor {

    @Accessor("defaultImpl")
    AmuletHolderCapability loliasm$defaultImplFastAccess();

}

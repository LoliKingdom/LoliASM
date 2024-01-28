package zone.rong.blahajasm.api;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 * {@link net.minecraft.item.ItemStack} implements this at runtime.
 *
 * This interface aids the initialization of capabilities in an on-demand manner.
 */
public interface IItemStackCapabilityInjector {

    CapabilityDispatcher getDispatcher();

    CapabilityDispatcher initDispatcher(ResourceLocation capabilityKey, ICapabilityProvider provider);

}

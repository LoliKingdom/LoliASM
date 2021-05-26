package zone.rong.loliasm.api;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import java.util.function.Supplier;

public interface ICapabilityDispatcherResolver {

    CapabilityDispatcher getDispatcher();

    CapabilityDispatcher initDispatcher(ResourceLocation capabilityKey, ICapabilityProvider provider);

}

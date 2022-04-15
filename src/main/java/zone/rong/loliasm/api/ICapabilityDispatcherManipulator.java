package zone.rong.loliasm.api;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

/**
 * {@link net.minecraftforge.common.capabilities.CapabilityDispatcher} implements this at runtime.
 *
 * This interface allows internal manipulation of capabilities, mainly injection and stripping of capabilities.
 */
public interface ICapabilityDispatcherManipulator {

    void injectCapability(String name, ICapabilityProvider provider);

    default void injectCapability(ResourceLocation name, ICapabilityProvider provider) {
        injectCapability(name.toString(), provider);
    }

    <T> void stripCapability(String name, Capability<T> capability, @Nullable EnumFacing facing, T capabilityInstance);

    default <T> void stripCapability(ResourceLocation name, Capability<T> capability, @Nullable EnumFacing facing, T capabilityInstance) {
        stripCapability(name.toString(), capability, facing, capabilityInstance);
    }

}

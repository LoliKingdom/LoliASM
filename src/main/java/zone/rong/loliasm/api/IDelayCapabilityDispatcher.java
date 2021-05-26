package zone.rong.loliasm.api;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;

public interface IDelayCapabilityDispatcher {

    void injectCapability(String name, ICapabilityProvider provider);

    default void injectCapability(ResourceLocation name, ICapabilityProvider provider) {
        injectCapability(name.toString(), provider);
    }

    <T> void stripCapability(String name, Capability<T> capability, @Nullable EnumFacing facing, T capabilityInstance);

    default <T> void stripCapability(ResourceLocation name, Capability<T> capability, @Nullable EnumFacing facing, T capabilityInstance) {
        stripCapability(name.toString(), capability, facing, capabilityInstance);
    }

}

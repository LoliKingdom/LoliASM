package zone.rong.garyasm.common.internal.mixins;

import com.google.common.collect.ImmutableMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import zone.rong.garyasm.api.IItemStackCapabilityInjector;
import zone.rong.garyasm.api.ICapabilityDispatcherManipulator;

import java.util.Collections;

@Mixin(value = ItemStack.class, remap = false)
public abstract class ItemStackMixin implements IItemStackCapabilityInjector {

	@Shadow private CapabilityDispatcher capabilities;

    @Override
    public CapabilityDispatcher getDispatcher() {
        return this.capabilities == null ? this.capabilities = new CapabilityDispatcher(Collections.emptyMap(), null) : this.capabilities;
    }

    @Override
    public CapabilityDispatcher initDispatcher(ResourceLocation capabilityKey, ICapabilityProvider provider) {
        if (this.capabilities == null) {
            return this.capabilities = new CapabilityDispatcher(ImmutableMap.of(capabilityKey, provider), null);
        }
        ((ICapabilityDispatcherManipulator) (Object) this.capabilities).injectCapability(capabilityKey, provider);
        return this.capabilities;
    }

}

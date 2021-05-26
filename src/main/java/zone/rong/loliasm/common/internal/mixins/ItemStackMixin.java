package zone.rong.loliasm.common.internal.mixins;

import com.google.common.collect.ImmutableMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import zone.rong.loliasm.api.ICapabilityDispatcherResolver;
import zone.rong.loliasm.api.IDelayCapabilityDispatcher;

import java.util.Collections;
import java.util.function.Supplier;

@Mixin(value = ItemStack.class, remap = false)
public class ItemStackMixin implements ICapabilityDispatcherResolver {

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
        ((IDelayCapabilityDispatcher) (Object) this.capabilities).injectCapability(capabilityKey, provider);
        return this.capabilities;
    }

}

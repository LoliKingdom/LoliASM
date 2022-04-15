package zone.rong.loliasm.common.capability.mixins;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.IRegistryDelegate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import zone.rong.loliasm.api.IItemStackCapabilityDelayer;

import javax.annotation.Nullable;

@Mixin(value = ItemStack.class, remap = false)
public abstract class ItemStackMixin implements IItemStackCapabilityDelayer {

	@Shadow private CapabilityDispatcher capabilities;
	@Shadow private NBTTagCompound capNBT;
	@Shadow private IRegistryDelegate<Item> delegate;

	@Unique private boolean initializedCapabilities = false;

	@Shadow @Nullable protected abstract Item getItemRaw();

    @Override
    public boolean hasInitializedCapabilities() {
        return initializedCapabilities;
    }

	@Override
    public void initializeCapabilities() {
		if (initializedCapabilities) {
			return;
		}
		initializedCapabilities = true;
		Item item = getItemRaw();
		if (item != null) {
			this.delegate = item.delegate;
			ICapabilityProvider provider = item.initCapabilities((ItemStack) (Object) this, this.capNBT);
			this.capabilities = net.minecraftforge.event.ForgeEventFactory.gatherCapabilities((ItemStack) (Object) this, provider);
			if (this.capNBT != null && this.capabilities != null) {
				this.capabilities.deserializeNBT(this.capNBT);
			}
		}
    }

    /**
     * @author PrototypeTrousers
     */
    @Overwrite
    @Nullable
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.util.EnumFacing facing) {
        if (!initializedCapabilities) {
			initializeCapabilities();
        }
        return this.capabilities == null ? null : this.capabilities.getCapability(capability, facing);
    }

	/**
	 * @author PrototypeTrousers
	 */
    @Overwrite
    public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability, @Nullable net.minecraft.util.EnumFacing facing) {
        if (!initializedCapabilities) {
			initializeCapabilities();
        }
        return !((ItemStack) (Object) this).isEmpty() && this.capabilities != null && this.capabilities.hasCapability(capability, facing);
    }

	/**
	 * @author PrototypeTrousers
	 */
	@Overwrite
	public boolean areCapsCompatible(ItemStack other) {
		if (!initializedCapabilities) {
			initializeCapabilities();
		}
		if (this.capabilities == null) {
			if (((ItemStackMixin) (Object) other).capabilities == null) {
				return true;
			} else {
				return ((ItemStackMixin) (Object) other).capabilities.areCompatible(null);
			}
		} else {
			return this.capabilities.areCompatible(((ItemStackMixin) (Object) other).capabilities);
		}
	}

	/**
	 * @author PrototypeTrousers
	 */
    @Overwrite
    private void forgeInit() {
        Item item = this.getItemRaw();
        if (item != null) {
            this.delegate = item.delegate;
        }
    }

}

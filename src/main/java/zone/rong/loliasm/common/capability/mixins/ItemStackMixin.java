package zone.rong.loliasm.common.capability.mixins;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.IRegistryDelegate;
import org.spongepowered.asm.mixin.*;
import zone.rong.loliasm.api.IItemStackCapabilityDelayer;

import javax.annotation.Nullable;

// TODO: do this without mixins, too invasive with too many Overwrites.
@Mixin(value = ItemStack.class)
public abstract class ItemStackMixin implements IItemStackCapabilityDelayer {

	@Shadow private boolean isEmpty;
	@Shadow private int stackSize;
	@Shadow int itemDamage;
	@Shadow private NBTTagCompound stackTagCompound;

	@Shadow(remap = false) private CapabilityDispatcher capabilities;
	@Shadow(remap = false) private NBTTagCompound capNBT;
	@Shadow(remap = false) private IRegistryDelegate<Item> delegate;

	@Unique private boolean initializedCapabilities = false;

	@Shadow public abstract int getAnimationsToGo();

	@Shadow(remap = false) @Nullable protected abstract Item getItemRaw();

	@Override
    public boolean hasInitializedCapabilities() {
        return initializedCapabilities;
    }

	/**
	 * @author PrototypeTrousers, Rongmario
	 */
	@Override
    public void initializeCapabilities() {
		if (initializedCapabilities) {
			return;
		}
		initializedCapabilities = true;
		Item item = getItemRaw();
		if (item != null) {
			this.capabilities = ForgeEventFactory.gatherCapabilities((ItemStack) (Object) this, item.initCapabilities((ItemStack) (Object) this, this.capNBT));
			if (this.capNBT != null && this.capabilities != null) {
				this.capabilities.deserializeNBT(this.capNBT);
			}
		}
    }

	/**
	 * @author Rongmario
	 */
	@Overwrite
	public ItemStack copy() {
		ItemStack stack = new ItemStack(getItemRaw(), this.stackSize, this.itemDamage, this.capabilities != null ? this.capabilities.serializeNBT() : this.capNBT);
		stack.setAnimationsToGo(this.getAnimationsToGo());
		if (this.stackTagCompound != null) {
			((ItemStackMixin) (Object) stack).stackTagCompound = this.stackTagCompound.copy();
		}
		return stack;
	}

	/**
	 * @author PrototypeTrousers, Rongmario
	 */
    @Overwrite(remap = false)
    public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability, @Nullable net.minecraft.util.EnumFacing facing) {
        if (!initializedCapabilities) {
			initializeCapabilities();
        }
        return !this.isEmpty && this.capabilities != null && this.capabilities.hasCapability(capability, facing);
    }

	/**
	 * @author PrototypeTrousers, Rongmario
	 */
	@Nullable
	@Overwrite(remap = false)
	public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.util.EnumFacing facing) {
		if (!initializedCapabilities) {
			initializeCapabilities();
		}
		return (this.isEmpty || this.capabilities == null) ? null : this.capabilities.getCapability(capability, facing);
	}

	/**
	 * @author PrototypeTrousers, Rongmario
	 */
	@Overwrite(remap = false)
	public boolean areCapsCompatible(ItemStack other) {
		if (!initializedCapabilities) {
			initializeCapabilities();
		}
		if (this.capabilities == null) {
			return ((ItemStackMixin) (Object) other).capabilities == null || ((ItemStackMixin) (Object) other).capabilities.areCompatible(null);
		}
		return this.capabilities.areCompatible(((ItemStackMixin) (Object) other).capabilities);
	}

	/**
	 * @author PrototypeTrousers
	 */
    @Overwrite(remap = false)
    private void forgeInit() {
        Item item = this.getItemRaw();
        if (item != null) {
            this.delegate = item.delegate;
        }
    }

}

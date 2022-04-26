package zone.rong.loliasm.common.capability.mixins;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
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
	 * @reason If capabilities isn't present, copy capNBT as it maybe cached from previous copies while capabilities isn't instantiated yet
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
	 * @author Rongmario
	 * @reason Same sort of reasoning for {@link ItemStackMixin#copy()} but for NBT saving instead
	 */
	@Overwrite
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		ResourceLocation resourcelocation = Item.REGISTRY.getNameForObject(getItemRaw());
		nbt.setString("id", resourcelocation == null ? "minecraft:air" : resourcelocation.toString());
		nbt.setByte("Count", (byte) this.stackSize);
		nbt.setShort("Damage", (short) this.itemDamage);
		if (this.stackTagCompound != null) {
			nbt.setTag("tag", this.stackTagCompound);
		}
		if (this.capabilities != null) {
			NBTTagCompound cnbt = this.capabilities.serializeNBT();
			if (!cnbt.isEmpty()) {
				nbt.setTag("ForgeCaps", cnbt);
			}
		} else if (this.capNBT != null) {
			if (!this.capNBT.isEmpty()) {
				nbt.setTag("ForgeCaps", this.capNBT);
			}
		}
		return nbt;
	}

	/**
	 * @author PrototypeTrousers, Rongmario
	 * @reason Only query capabilities when needed
	 */
    @Overwrite(remap = false)
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (this.isEmpty) {
			return false;
		}
        if (!initializedCapabilities) {
			initializeCapabilities();
        }
        return this.capabilities != null && this.capabilities.hasCapability(capability, facing);
    }

	/**
	 * @author PrototypeTrousers, Rongmario
	 * @reason Only query capabilities when needed
	 */
	@Nullable
	@Overwrite(remap = false)
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (this.isEmpty) {
			return null;
		}
		if (!initializedCapabilities) {
			initializeCapabilities();
		}
		return this.capabilities == null ? null : this.capabilities.getCapability(capability, facing);
	}

	/**
	 * @author PrototypeTrousers, Rongmario
	 * @reason Only query capabilities when needed
	 */
	@Overwrite(remap = false)
	public boolean areCapsCompatible(ItemStack other) {
		if (!initializedCapabilities) {
			initializeCapabilities();
		}
		if (!((ItemStackMixin) (Object) other).initializedCapabilities) {
			((ItemStackMixin) (Object) other).initializeCapabilities();
		}
		if (this.capabilities == null) {
			return ((ItemStackMixin) (Object) other).capabilities == null || ((ItemStackMixin) (Object) other).capabilities.areCompatible(null);
		}
		return this.capabilities.areCompatible(((ItemStackMixin) (Object) other).capabilities);
	}

	/**
	 * @author PrototypeTrousers
	 * @reason Only query capabilities when needed
	 */
    @Overwrite(remap = false)
    private void forgeInit() {
        Item item = this.getItemRaw();
        if (item != null) {
            this.delegate = item.delegate;
        }
    }

}

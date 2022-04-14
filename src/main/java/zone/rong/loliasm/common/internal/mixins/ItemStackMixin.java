package zone.rong.loliasm.common.internal.mixins;

import com.google.common.collect.ImmutableMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.IRegistryDelegate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import zone.rong.loliasm.api.ICapabilityDispatcherResolver;
import zone.rong.loliasm.api.IDelayCapabilityDispatcher;

import javax.annotation.Nullable;
import java.util.Collections;

@Mixin( value = ItemStack.class, remap = false )
public abstract class ItemStackMixin implements ICapabilityDispatcherResolver
{

	public boolean initedCaps = false;
	@Shadow
	private NBTTagCompound capNBT;
	@Shadow
	private CapabilityDispatcher capabilities;
	@Shadow
	private IRegistryDelegate<Item> delegate;

	@Shadow @Nullable protected abstract Item getItemRaw();

	@Override
	public CapabilityDispatcher getCap()
	{
		return this.capabilities;
	}

	@Override
	public CapabilityDispatcher getDispatcher()
	{
		return this.capabilities == null ? this.capabilities = new CapabilityDispatcher( Collections.emptyMap(), null ) : this.capabilities;
	}

	@Override
	public CapabilityDispatcher initDispatcher( ResourceLocation capabilityKey, ICapabilityProvider provider )
	{
		if( this.capabilities == null )
		{
			return this.capabilities = new CapabilityDispatcher( ImmutableMap.of( capabilityKey, provider ), null );
		}
		( (IDelayCapabilityDispatcher) (Object) this.capabilities ).injectCapability( capabilityKey, provider );
		return this.capabilities;
	}

	/**
	 * @author
	 */
	@Overwrite
	@Nullable
	public <T> T getCapability( net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.util.EnumFacing facing )
	{
		if( !initedCaps )
		{
			initCaps();
		}
		return this.capabilities == null ? null : this.capabilities.getCapability( capability, facing );
	}

	/**
	 * @author
	 */
	@Overwrite
	public boolean hasCapability( net.minecraftforge.common.capabilities.Capability<?> capability, @Nullable net.minecraft.util.EnumFacing facing )
	{
		if( !initedCaps )
		{
			initCaps();
		}
		return !( (ItemStack) (Object) this ).isEmpty() && this.capabilities != null && this.capabilities.hasCapability( capability, facing );
	}

	/**
	 * @author
	 */
	@Overwrite
	public boolean areCapsCompatible( ItemStack other )
	{
		if( !initedCaps )
		{
			initCaps();
		}
		if( this.capabilities == null )
		{
			if( !( (ICapabilityDispatcherResolver) (Object) other ).isInited() )
			{
				( (ICapabilityDispatcherResolver) (Object) other ).initCaps();
			}
			if( ( (ICapabilityDispatcherResolver) (Object) other ).getCap() == null )
			{
				return true;
			}
			else
			{
				return ( (ICapabilityDispatcherResolver) (Object) other ).getCap().areCompatible( null );
			}
		}
		else
		{
			if( !( (ICapabilityDispatcherResolver) (Object) other ).isInited() )
			{
				( (ICapabilityDispatcherResolver) (Object) other ).initCaps();
			}
			return this.capabilities.areCompatible( ( (ICapabilityDispatcherResolver) (Object) other ).getCap() );
		}
	}


	/**
	 * @author
	 */
	@Overwrite
	private void forgeInit()
	{
		Item item = this.getItemRaw();
		if( item != null )
		{
			this.delegate = item.delegate;
		}
	}

	public void initCaps()
	{
		initedCaps = true;
		Item item = this.getItemRaw();
		if ( item != null )
		{
			ICapabilityProvider provider = item.initCapabilities( (ItemStack) (Object) this, this.capNBT );
			this.capabilities = ForgeEventFactory.gatherCapabilities( ( (ItemStack) (Object) this ), provider );
			if( this.capNBT != null && this.capabilities != null )
			{
				this.capabilities.deserializeNBT( this.capNBT );
			}
		}
	}

	@Override
	public Boolean isInited()
	{
		return initedCaps;
	}
}

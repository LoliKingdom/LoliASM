package zone.rong.blahajasm.common.internal.mixins;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import zone.rong.blahajasm.BlahajLogger;
import zone.rong.blahajasm.api.ICapabilityDispatcherManipulator;

import javax.annotation.Nullable;
import java.util.Arrays;

@Mixin(value = CapabilityDispatcher.class, remap = false)
public class CapabilityDispatcherMixin implements ICapabilityDispatcherManipulator {

	@Shadow private ICapabilityProvider[] caps;
	@Shadow private INBTSerializable<NBTBase>[] writers;
	@Shadow private String[] names;

	@Override
	public void injectCapability(String name, ICapabilityProvider provider) {
		this.caps = ArrayUtils.add(this.caps, provider);
		if (provider instanceof INBTSerializable) {
			this.writers = ArrayUtils.add(this.writers, (INBTSerializable<NBTBase>) provider);
			this.names = ArrayUtils.add(this.names, name);
		}
	}

	@Override
	public <T> void stripCapability(String name, Capability<T> capability, @Nullable EnumFacing facing, T capabilityInstance) {
		ICapabilityProvider provider = null;
		for (ICapabilityProvider p : this.caps) {
			if (p.getCapability(capability, facing) == capabilityInstance) {
				provider = p;
			}
		}
		if (provider == null) {
			BlahajLogger.instance.error("{}@{} does not exist in {}", name, capability.getName(), Arrays.toString(this.caps));
			return;
		}
		this.caps = ArrayUtils.removeElement(this.caps, provider);
		if (provider instanceof INBTSerializable) {
			this.writers = ArrayUtils.removeElement(this.writers, provider);
			this.names = ArrayUtils.removeElement(this.names, name);
		}
	}

}
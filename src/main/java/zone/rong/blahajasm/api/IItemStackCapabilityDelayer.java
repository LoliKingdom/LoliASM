package zone.rong.blahajasm.api;

/**
 * {@link net.minecraft.item.ItemStack} implements this at runtime.
 *
 * This interface aids the delaying of capabilities initialization if {@link zone.rong.blahajasm.config.BlahajConfig#delayItemStackCapabilityInit} == true
 */
public interface IItemStackCapabilityDelayer {

    boolean hasInitializedCapabilities();

    /**
     * Can only run when {@link IItemStackCapabilityDelayer#hasInitializedCapabilities()} == true
     */
    void initializeCapabilities();

}

package zone.rong.blahajasm.common.registries.mixins;

import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistrySimple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import zone.rong.blahajasm.api.mixins.RegistrySimpleExtender;

import java.util.Map;

@Mixin(SoundRegistry.class)
public abstract class SoundRegistryMixin extends RegistrySimple<ResourceLocation, SoundEventAccessor> implements RegistrySimpleExtender {

    /**
     * @author Rongmario
     * @reason Use RegistrySimple#createUnderlyingMap
     */
    @Overwrite
    protected Map<ResourceLocation, SoundEventAccessor> createUnderlyingMap() {
        return super.createUnderlyingMap();
    }

    /**
     * @author Rongmario
     * @reason Use RegistrySimpleExtender#clearUnderlyingMap
     */
    @Overwrite
    public void clearMap() {
        this.clearUnderlyingMap();
    }

}

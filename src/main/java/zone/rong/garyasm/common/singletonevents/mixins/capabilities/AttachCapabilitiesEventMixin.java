package zone.rong.garyasm.common.singletonevents.mixins.capabilities;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import org.spongepowered.asm.mixin.*;
import zone.rong.garyasm.common.singletonevents.IRefreshEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

@Mixin(value = AttachCapabilitiesEvent.class, remap = false)
public class AttachCapabilitiesEventMixin<T> extends Event implements IRefreshEvent {

    @Shadow @Final @Mutable private T obj;
    @Shadow @Final @Mutable private Map<ResourceLocation, ICapabilityProvider> caps = new Object2ObjectArrayMap<>(1);

    @Unique private EventPriority garyPriority = null;

    @Override
    public void beforeAttachCapabilities(Object data) {
        this.obj = (T) data;
        this.caps.clear();
    }

    @Nullable
    @Override
    public EventPriority getPhase() {
        return garyPriority;
    }

    @Override
    public void setPhase(@Nonnull EventPriority next) {
        this.garyPriority = next;
    }

}

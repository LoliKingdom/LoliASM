package zone.rong.loliasm.common.singletonevents.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import zone.rong.loliasm.common.singletonevents.IRefreshEvent;

import javax.annotation.Nullable;

@Mixin(value = ForgeEventFactory.class, remap = false)
public abstract class ForgeEventFactoryMixin {

    @Shadow
    @Nullable
    private static CapabilityDispatcher gatherCapabilities(AttachCapabilitiesEvent<?> event, @Nullable ICapabilityProvider parent) {
        throw new AssertionError();
    }

    @Unique private static final AttachCapabilitiesEvent<TileEntity> TE_ATTACH_CAPABILITIES_EVENT = new AttachCapabilitiesEvent<>(TileEntity.class, null);
    @Unique private static final AttachCapabilitiesEvent<Entity> ENTITY_ATTACH_CAPABILITIES_EVENT = new AttachCapabilitiesEvent<>(Entity.class, null);
    @Unique private static final AttachCapabilitiesEvent<ItemStack> ITEM_STACK_ATTACH_CAPABILITIES_EVENT = new AttachCapabilitiesEvent<>(ItemStack.class, null);
    @Unique private static final AttachCapabilitiesEvent<Chunk> CHUNK_ATTACH_CAPABILITIES_EVENT = new AttachCapabilitiesEvent<>(Chunk.class, null);
    @Unique private static final IRefreshEvent TE_ATTACH_CAPABILITIES_EVENT_CASTED = (IRefreshEvent) TE_ATTACH_CAPABILITIES_EVENT;
    @Unique private static final IRefreshEvent ENTITY_ATTACH_CAPABILITIES_EVENT_CASTED = (IRefreshEvent) TE_ATTACH_CAPABILITIES_EVENT;
    @Unique private static final IRefreshEvent ITEM_STACK_ATTACH_CAPABILITIES_EVENT_CASTED = (IRefreshEvent) TE_ATTACH_CAPABILITIES_EVENT;
    @Unique private static final IRefreshEvent CHUNK_ATTACH_CAPABILITIES_EVENT_CASTED = (IRefreshEvent) TE_ATTACH_CAPABILITIES_EVENT;

    // Not frequent enough:
    /*
        @Unique private static final AttachCapabilitiesEvent<Village> VILLAGE_ATTACH_CAPABILITIES_EVENT = new AttachCapabilitiesEvent<>(Village.class, null);
        @Unique private static final AttachCapabilitiesEvent<World> WORLD_ATTACH_CAPABILITIES_EVENT = new AttachCapabilitiesEvent<>(World.class, null);
    */

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Nullable
    @Overwrite
    public static CapabilityDispatcher gatherCapabilities(TileEntity tileEntity) {
        TE_ATTACH_CAPABILITIES_EVENT_CASTED.refresh(tileEntity);
        return gatherCapabilities(TE_ATTACH_CAPABILITIES_EVENT, null);
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Nullable
    @Overwrite
    public static CapabilityDispatcher gatherCapabilities(Entity entity) {
        ENTITY_ATTACH_CAPABILITIES_EVENT_CASTED.refresh(entity);
        return gatherCapabilities(ENTITY_ATTACH_CAPABILITIES_EVENT, null);
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Nullable
    @Overwrite
    public static CapabilityDispatcher gatherCapabilities(ItemStack stack, ICapabilityProvider parent) {
        ITEM_STACK_ATTACH_CAPABILITIES_EVENT_CASTED.refresh(stack);
        return gatherCapabilities(ITEM_STACK_ATTACH_CAPABILITIES_EVENT, parent);
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Nullable
    @Overwrite
    public static CapabilityDispatcher gatherCapabilities(Chunk chunk) {
        CHUNK_ATTACH_CAPABILITIES_EVENT_CASTED.refresh(chunk);
        return gatherCapabilities(CHUNK_ATTACH_CAPABILITIES_EVENT, null);
    }

}

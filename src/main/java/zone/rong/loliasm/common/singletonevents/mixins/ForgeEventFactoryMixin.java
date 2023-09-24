package zone.rong.loliasm.common.singletonevents.mixins;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import zone.rong.loliasm.common.singletonevents.IRefreshEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Map;

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
    @Unique private static final IRefreshEvent ENTITY_ATTACH_CAPABILITIES_EVENT_CASTED = (IRefreshEvent) ENTITY_ATTACH_CAPABILITIES_EVENT;
    @Unique private static final IRefreshEvent ITEM_STACK_ATTACH_CAPABILITIES_EVENT_CASTED = (IRefreshEvent) ITEM_STACK_ATTACH_CAPABILITIES_EVENT;
    @Unique private static final IRefreshEvent CHUNK_ATTACH_CAPABILITIES_EVENT_CASTED = (IRefreshEvent) CHUNK_ATTACH_CAPABILITIES_EVENT;

    @Unique private static final NeighborNotifyEvent NEIGHBOR_NOTIFY_EVENT = new NeighborNotifyEvent(null, null, null, null, false);
    @Unique private static final IRefreshEvent NEIGHBOR_NOTIFY_EVENT_CASTED = (IRefreshEvent) NEIGHBOR_NOTIFY_EVENT;

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
        TE_ATTACH_CAPABILITIES_EVENT_CASTED.beforeAttachCapabilities(tileEntity);
        MinecraftForge.EVENT_BUS.post(TE_ATTACH_CAPABILITIES_EVENT);
        Map<ResourceLocation, ICapabilityProvider> caps = TE_ATTACH_CAPABILITIES_EVENT.getCapabilities();
        return !caps.isEmpty() ? new CapabilityDispatcher(caps, null) : null;
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Nullable
    @Overwrite
    public static CapabilityDispatcher gatherCapabilities(Entity entity) {
        ENTITY_ATTACH_CAPABILITIES_EVENT_CASTED.beforeAttachCapabilities(entity);
        MinecraftForge.EVENT_BUS.post(ENTITY_ATTACH_CAPABILITIES_EVENT);
        Map<ResourceLocation, ICapabilityProvider> caps = ENTITY_ATTACH_CAPABILITIES_EVENT.getCapabilities();
        return !caps.isEmpty() ? new CapabilityDispatcher(caps, null) : null;
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Nullable
    @Overwrite
    public static CapabilityDispatcher gatherCapabilities(ItemStack stack, ICapabilityProvider parent) {
        ITEM_STACK_ATTACH_CAPABILITIES_EVENT_CASTED.beforeAttachCapabilities(stack);
        MinecraftForge.EVENT_BUS.post(ITEM_STACK_ATTACH_CAPABILITIES_EVENT);
        Map<ResourceLocation, ICapabilityProvider> caps = ITEM_STACK_ATTACH_CAPABILITIES_EVENT.getCapabilities();
        return parent != null || !caps.isEmpty() ? new CapabilityDispatcher(caps, parent) : null;
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Nullable
    @Overwrite
    public static CapabilityDispatcher gatherCapabilities(Chunk chunk) {
        CHUNK_ATTACH_CAPABILITIES_EVENT_CASTED.beforeAttachCapabilities(chunk);
        MinecraftForge.EVENT_BUS.post(CHUNK_ATTACH_CAPABILITIES_EVENT);
        Map<ResourceLocation, ICapabilityProvider> caps = CHUNK_ATTACH_CAPABILITIES_EVENT.getCapabilities();
        return !caps.isEmpty() ? new CapabilityDispatcher(caps, null) : null;
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Nullable
    @Overwrite
    public static NeighborNotifyEvent onNeighborNotify(World world, BlockPos pos, IBlockState state, EnumSet<EnumFacing> notifiedSides, boolean forceRedstoneUpdate) {
        NEIGHBOR_NOTIFY_EVENT_CASTED.beforeBlockEvent(world, pos, state);
        NEIGHBOR_NOTIFY_EVENT_CASTED.beforeNeighborNotify(notifiedSides, forceRedstoneUpdate);
        MinecraftForge.EVENT_BUS.post(NEIGHBOR_NOTIFY_EVENT);
        return NEIGHBOR_NOTIFY_EVENT;
    }

}

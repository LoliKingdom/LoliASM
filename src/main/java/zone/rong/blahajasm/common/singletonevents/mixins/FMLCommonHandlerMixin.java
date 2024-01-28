package zone.rong.blahajasm.common.singletonevents.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.gameevent.TickEvent.*;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import zone.rong.blahajasm.common.singletonevents.IRefreshEvent;

@Mixin(value = FMLCommonHandler.class, remap = false)
public abstract class FMLCommonHandlerMixin {

    @Shadow @Deprecated public abstract EventBus bus();

    @Unique private static final ClientTickEvent START_CLIENT_TICK_EVENT = new ClientTickEvent(Phase.START);
    @Unique private static final ClientTickEvent END_CLIENT_TICK_EVENT = new ClientTickEvent(Phase.END);

    @Unique private static final ServerTickEvent START_SERVER_TICK_EVENT = new ServerTickEvent(Phase.START);
    @Unique private static final ServerTickEvent END_SERVER_TICK_EVENT = new ServerTickEvent(Phase.END);

    @Unique private static final WorldTickEvent START_WORLD_TICK_EVENT = new WorldTickEvent(Side.SERVER, Phase.START, null);
    @Unique private static final WorldTickEvent END_WORLD_TICK_EVENT = new WorldTickEvent(Side.SERVER, Phase.END, null);
    @Unique private static final IRefreshEvent START_WORLD_TICK_EVENT_CASTED = (IRefreshEvent) START_WORLD_TICK_EVENT;
    @Unique private static final IRefreshEvent END_WORLD_TICK_EVENT_CASTED = (IRefreshEvent) END_WORLD_TICK_EVENT;

    @Unique private static final PlayerTickEvent CLIENT_START_PLAYER_TICK_EVENT = new PlayerTickEvent(Phase.START, null);
    @Unique private static final PlayerTickEvent CLIENT_END_PLAYER_TICK_EVENT = new PlayerTickEvent(Phase.END, null);

    @Unique private static final PlayerTickEvent SERVER_START_PLAYER_TICK_EVENT = new PlayerTickEvent(Phase.START, null);
    @Unique private static final PlayerTickEvent SERVER_END_PLAYER_TICK_EVENT = new PlayerTickEvent(Phase.END, null);

    @Unique private static final IRefreshEvent CLIENT_START_PLAYER_TICK_EVENT_CASTED = (IRefreshEvent) CLIENT_START_PLAYER_TICK_EVENT;
    @Unique private static final IRefreshEvent CLIENT_END_PLAYER_TICK_EVENT_CASTED = (IRefreshEvent) CLIENT_END_PLAYER_TICK_EVENT;
    @Unique private static final IRefreshEvent SERVER_START_PLAYER_TICK_EVENT_CASTED = ((IRefreshEvent) SERVER_START_PLAYER_TICK_EVENT).setTickSide(Side.SERVER);
    @Unique private static final IRefreshEvent SERVER_END_PLAYER_TICK_EVENT_CASTED = ((IRefreshEvent) SERVER_END_PLAYER_TICK_EVENT).setTickSide(Side.SERVER);

    @Unique private static final RenderTickEvent START_RENDER_TICK_EVENT = new RenderTickEvent(Phase.START, 0F);
    @Unique private static final RenderTickEvent END_RENDER_TICK_EVENT = new RenderTickEvent(Phase.END, 0F);
    @Unique private static final IRefreshEvent START_RENDER_TICK_EVENT_CASTED = (IRefreshEvent) START_RENDER_TICK_EVENT;

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Overwrite
    public void onPostServerTick() {
        bus().post(END_SERVER_TICK_EVENT);
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Overwrite
    public void onPostWorldTick(World world) {
        END_WORLD_TICK_EVENT_CASTED.beforeWorldTick(world);
        bus().post(END_WORLD_TICK_EVENT);
        END_WORLD_TICK_EVENT_CASTED.afterWorldTick();
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Overwrite
    public void onPreServerTick() {
        bus().post(START_SERVER_TICK_EVENT);
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Overwrite
    public void onPreWorldTick(World world) {
        START_WORLD_TICK_EVENT_CASTED.beforeWorldTick(world);
        bus().post(START_WORLD_TICK_EVENT);
        START_WORLD_TICK_EVENT_CASTED.afterWorldTick();
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Overwrite
    public void onPreClientTick() {
        bus().post(START_CLIENT_TICK_EVENT);
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Overwrite
    public void onPostClientTick() {
        bus().post(END_CLIENT_TICK_EVENT);
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Overwrite
    public void onRenderTickStart(float timer) {
        START_RENDER_TICK_EVENT_CASTED.beforeRenderTick(timer);
        bus().post(START_RENDER_TICK_EVENT);
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Overwrite
    public void onRenderTickEnd(float timer) {
        bus().post(END_RENDER_TICK_EVENT);
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Overwrite
    public void onPlayerPreTick(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            SERVER_START_PLAYER_TICK_EVENT_CASTED.beforePlayerTick(player);
            bus().post(SERVER_START_PLAYER_TICK_EVENT);
            SERVER_START_PLAYER_TICK_EVENT_CASTED.afterPlayerTick();
        } else {
            CLIENT_START_PLAYER_TICK_EVENT_CASTED.beforePlayerTick(player);
            bus().post(CLIENT_START_PLAYER_TICK_EVENT);
            CLIENT_START_PLAYER_TICK_EVENT_CASTED.afterPlayerTick();
        }
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Overwrite
    public void onPlayerPostTick(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            SERVER_END_PLAYER_TICK_EVENT_CASTED.beforePlayerTick(player);
            bus().post(SERVER_END_PLAYER_TICK_EVENT);
            SERVER_END_PLAYER_TICK_EVENT_CASTED.afterPlayerTick();
        } else {
            CLIENT_END_PLAYER_TICK_EVENT_CASTED.beforePlayerTick(player);
            bus().post(CLIENT_END_PLAYER_TICK_EVENT);
            CLIENT_END_PLAYER_TICK_EVENT_CASTED.afterPlayerTick();
        }
    }

}

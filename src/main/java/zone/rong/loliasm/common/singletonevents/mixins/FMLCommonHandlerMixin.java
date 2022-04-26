package zone.rong.loliasm.common.singletonevents.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.Animation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.*;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import zone.rong.loliasm.common.singletonevents.IRefreshEvent;

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
        Animation.setClientPartialTickTime(timer);
        bus().post(new TickEvent.RenderTickEvent(Phase.START, timer));
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Overwrite
    public void onRenderTickEnd(float timer) {
        bus().post(new TickEvent.RenderTickEvent(Phase.END, timer));
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Overwrite
    public void onPlayerPreTick(EntityPlayer player) {
        bus().post(new TickEvent.PlayerTickEvent(Phase.START, player));
    }

    /**
     * @author Rongmario
     * @reason Singleton Haven
     */
    @Overwrite
    public void onPlayerPostTick(EntityPlayer player) {
        bus().post(new TickEvent.PlayerTickEvent(Phase.END, player));
    }

}

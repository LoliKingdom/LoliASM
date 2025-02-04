package zone.rong.garyasm.common.singletonevents.mixins.gameevent;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import zone.rong.garyasm.common.singletonevents.IRefreshEvent;

@Mixin(value = PlayerTickEvent.class, remap = true)
public class PlayerTickEventMixin implements IRefreshEvent {

    @Shadow @Final @Mutable public EntityPlayer player;

    @Override
    public void beforePlayerTick(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public void afterPlayerTick() {
        this.player = null;
    }

}

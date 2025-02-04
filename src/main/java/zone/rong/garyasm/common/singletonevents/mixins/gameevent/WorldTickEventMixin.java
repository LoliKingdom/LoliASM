package zone.rong.loliasm.common.singletonevents.mixins.gameevent;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import zone.rong.loliasm.common.singletonevents.IRefreshEvent;

@Mixin(value = WorldTickEvent.class, remap = false)
public class WorldTickEventMixin implements IRefreshEvent {

    @Shadow @Final @Mutable public World world;

    @Override
    public void beforeWorldTick(World world) {
        this.world = world;
    }

    @Override
    public void afterWorldTick() {
        this.world = null;
    }

}

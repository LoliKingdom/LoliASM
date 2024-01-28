package zone.rong.blahajasm.common.forgefixes.mixins;

import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ForgeEventFactory.class, remap = false)
public class ForgeEventFactoryMixin {

    @SuppressWarnings("ConstantConditions")
    @Redirect(method = "onBucketUse", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/player/FillBucketEvent;getResult()Lnet/minecraftforge/fml/common/eventhandler/Event$Result;"))
    private static Event.Result checkContract(FillBucketEvent event) {
        if (event.getResult() == Event.Result.ALLOW && event.getFilledBucket() == null) {
            return Event.Result.DEFAULT;
        }
        return event.getResult();
    }

}

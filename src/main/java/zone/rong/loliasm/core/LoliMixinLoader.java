package zone.rong.loliasm.core;

import org.spongepowered.asm.mixin.Mixins;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.mixinbooter.MixinLoader;

@MixinLoader
public class LoliMixinLoader {

    {
        if (LoliConfig.getConfig().bakedQuadsSquasher) {
            Mixins.addConfiguration("mixins.bakedquadsquasher.json");
        }
    }

}

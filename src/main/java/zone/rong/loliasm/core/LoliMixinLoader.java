package zone.rong.loliasm.core;

import org.spongepowered.asm.mixin.Mixins;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.mixinbooter.MixinLoader;

@MixinLoader
public class LoliMixinLoader {

    {
        LoliConfig.Data data = LoliConfig.getConfig();
        if (data.bakedQuadsSquasher) {
            Mixins.addConfiguration("mixins.bakedquadsquasher.json");
        }
        if (data.modFixes) {
            Mixins.addConfiguration("mixins.modfixes.json");
        }
    }

}

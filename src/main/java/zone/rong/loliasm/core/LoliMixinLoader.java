package zone.rong.loliasm.core;

import org.spongepowered.asm.mixin.Mixins;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.mixinbooter.MixinLoader;

@MixinLoader
public class LoliMixinLoader {

    {
        if (LoliConfig.instance.squashBakedQuads) {
            Mixins.addConfiguration("mixins.bakedquadsquasher.json");
        }
        if (LoliConfig.instance.fixBlockIEBaseArrayIndexOutOfBoundsException) {
            Mixins.addConfiguration("mixins.modfixes.json");
        }
    }

}

package zone.rong.loliasm.core;

import org.spongepowered.asm.mixin.Mixins;
import zone.rong.mixinbooter.MixinLoader;

@MixinLoader
public class LoliMixinLoader {

    {
        Mixins.addConfiguration("mixins.bakedquadsquasher.json");
    }

}

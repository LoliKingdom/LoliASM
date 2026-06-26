package zone.rong.loliasm;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import zone.rong.loliasm.core.LoliLoadingPlugin;

@Mod(modid = "chibi", name = "Chibi", version = LoliLoadingPlugin.VERSION, dependencies = "required-after:mixinbooter@[10.7,);after:jei;after:spark@[1.5.2]")
public class Chibi {

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        Loader.instance().getIndexedModList().get("loliasm").getMetadata().parent = "chibi";
    }

}

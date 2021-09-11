package zone.rong.loliasm.common.internal.mixins;

import com.google.common.base.Strings;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.ThreadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(LoadController.class)
public abstract class LoadControllerMixin {

    @Shadow(remap = false) private ModContainer activeContainer;

    @Shadow(remap = false) @Nullable protected abstract ModContainer findActiveContainerFromStack();

    /**
     * @author Rongmario
     * @reason Allow a faster lookup through ThreadContext first as some contexts submit modIds through this way
     */
    @Nullable
    @Overwrite(remap = false)
    public ModContainer activeContainer() {
        if (activeContainer == null) {
            String modId = ThreadContext.get("mod");
            if (Strings.isNullOrEmpty(modId)) {
                return findActiveContainerFromStack();
            }
            ModContainer container = Loader.instance().getIndexedModList().get(modId);
            return container == null ? findActiveContainerFromStack() : container;
        }
        return activeContainer;
    }

}

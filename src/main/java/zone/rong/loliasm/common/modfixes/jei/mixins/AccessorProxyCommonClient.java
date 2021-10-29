package zone.rong.loliasm.common.modfixes.jei.mixins;

import mezz.jei.startup.JeiStarter;
import mezz.jei.startup.ProxyCommonClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ProxyCommonClient.class, remap = false)
public interface AccessorProxyCommonClient {

    @Accessor(value = "starter")
    JeiStarter getJeiStarter();

}

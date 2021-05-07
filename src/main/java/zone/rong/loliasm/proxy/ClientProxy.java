package zone.rong.loliasm.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import zone.rong.loliasm.client.models.conditions.CanonicalConditions;

public class ClientProxy extends CommonProxy {

    @Override
    @SuppressWarnings("deprecation")
    public void preInit(FMLPreInitializationEvent event) {
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(manager -> {
            CanonicalConditions.destroyCache();
            // MultipartBakedModelCache.destroyCache();
        });
    }
}

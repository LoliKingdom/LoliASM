package zone.rong.loliasm.client.mcfixes.mixins.mc186052;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.config.LoliConfig;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/FMLClientHandler;handleClientWorldClosing(Lnet/minecraft/client/multiplayer/WorldClient;)V",
                    remap = false))
    private void injectLoadWorld(@Nullable WorldClient worldClientIn, String loadingMessage, CallbackInfo ci) {
        if (LoliConfig.instance.fixMC186052) {
            Map<ResourceLocation, ITextureObject> textureObjects = ((TextureManagerExpansion) Minecraft.getMinecraft().getTextureManager()).getMapTextureObjects();
            if (textureObjects != null) {
                int count = 0;
                Iterator<Entry<ResourceLocation, ITextureObject>> entryIter = textureObjects.entrySet().iterator();
                while (entryIter.hasNext()) {
                    Entry<ResourceLocation, ITextureObject> entry = entryIter.next();
                    if (entry.getValue() instanceof ThreadDownloadImageData) {
                        if (entry.getKey().getPath().startsWith("skins/")) {
                            ((ThreadDownloadImageData) entry.getValue()).deleteGlTexture();
                            entryIter.remove();
                            LoliLogger.instance.debug("Released {} texture", entry.getKey());
                            count++;
                        }
                    }
                }
                LoliLogger.instance.info("Released {} skin textures", count);
            }
        }
    }

}

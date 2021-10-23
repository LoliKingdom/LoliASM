package zone.rong.loliasm.common.modfixes.jei.mixins;

import com.google.common.base.Stopwatch;
import com.google.common.io.Files;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import me.nallar.whocalled.WhoCalled;
import mezz.jei.Internal;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.startup.JeiStarter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.rong.loliasm.LoliASM;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.api.mixins.IngredientFilterExtender;
import zone.rong.loliasm.core.LoliHooks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.CompletableFuture;

@Mixin(value = Internal.class, remap = false)
public class InternalMixin {

    @Inject(method = "setIngredientFilter", at = @At("RETURN"))
    private static void onSetIngredientFilter(IngredientFilter ingredientFilter, CallbackInfo ci) {
        if (WhoCalled.$.isCalledByClass(JeiStarter.class)) {
            for (Char2ObjectMap.Entry<String> entry : LoliHooks.JEI.treeIdentifiers.char2ObjectEntrySet()) {
                File cacheFolder = new File(LoliASM.proxy.loliCachesFolder, "jei");
                cacheFolder.mkdir();
                File cache = new File(cacheFolder, entry.getValue() + "_tree.bin");
                if (!cache.exists() || !LoliASM.proxy.consistentModList) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            Stopwatch stopwatch = Stopwatch.createStarted();
                            File temp = File.createTempFile(entry.getValue(), ".bin");
                            temp.deleteOnExit();
                            FileOutputStream fileOutputStream = new FileOutputStream(temp);
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                            objectOutputStream.writeObject(((IngredientFilterExtender) ingredientFilter).getTree(entry.getCharKey()));
                            objectOutputStream.close();
                            fileOutputStream.close();
                            Files.move(temp, cache);
                            LoliLogger.instance.info("{} Search Tree took {} to be serialized.", entry.getValue(), stopwatch.stop());
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            }
        }
    }

}

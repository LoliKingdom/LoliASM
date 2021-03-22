package zone.rong.loliasm;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.config.Config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LoliConfig {

    public static Data config;

    public static class Data {

        @Config.Ignore
        final String softPatchComment = "Not so invasive patches, this will work 99% of the time in a large modpack.";
        public final boolean softPatch;

        @Config.Ignore
        final String hardPatchComment = "Super invasive patches, EXPERIMENTAL.";
        public final boolean hardPatch;

        @Config.Ignore
        final String logClassesThatNeedPatchingComment = "Turn this on to log any callers using deprecated BakedQuad constructors. If you do see any classes, put them in the list below.";
        public final boolean logClassesThatNeedPatching;

        @Config.Ignore
        final String hardPatchClassesComment = "Classes that needs their BakedQuad::new calls redirected. In layman terms, if you see a crash, grab the class name, and slot it here...";
        public final String[] hardPatchClasses;

        public Data(boolean softPatch, boolean hardPatch, boolean logClassesThatNeedPatching, String[] hardPatchClasses) {
            this.softPatch = softPatch;
            this.hardPatch = hardPatch;
            this.logClassesThatNeedPatching = logClassesThatNeedPatching;
            this.hardPatchClasses = hardPatchClasses;
        }
    }

    public static Data initConfig() throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .addDeserializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getAnnotation(Config.Ignore.class) != null;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();
        Data data;
        File configFile = new File(Launch.minecraftHome, "config/loliasm.json");
        if (!configFile.exists()) {
            try (FileWriter writer = new FileWriter(configFile)) {
                data = new Data(true, false, false, new String[] { "net.minecraft.client.renderer.block.model.FaceBakery" });
                gson.toJson(data, writer);
            }
        } else {
            try (FileReader reader = new FileReader(configFile)) {
                data = config = gson.fromJson(reader, Data.class);
            }
        }
        return data;
    }

}

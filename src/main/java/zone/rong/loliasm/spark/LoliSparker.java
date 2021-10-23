package zone.rong.loliasm.spark;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import me.lucko.spark.common.SparkPlatform;
import me.lucko.spark.common.command.sender.CommandSender;
import me.lucko.spark.common.platform.AbstractPlatformInfo;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.sampler.Sampler;
import me.lucko.spark.common.sampler.ThreadDumper;
import me.lucko.spark.common.sampler.ThreadGrouper;
import me.lucko.spark.common.sampler.ThreadNodeOrder;
import me.lucko.spark.common.sampler.async.AsyncProfilerAccess;
import me.lucko.spark.common.sampler.async.AsyncSampler;
import me.lucko.spark.common.sampler.java.JavaSampler;
import me.lucko.spark.common.sampler.node.MergeMode;
import me.lucko.spark.common.util.MethodDisambiguator;
import me.lucko.spark.lib.adventure.text.Component;
import me.lucko.spark.lib.okhttp3.MediaType;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.config.LoliConfig;
import zone.rong.loliasm.core.LoliLoadingPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoliSparker {

    private static PlatformInfo platformInfo = new LoliPlatformInfo();
    private static CommandSender commandSender = new LoliCommandSender();
    private static Map<String, Sampler> ongoingSamplers = new Object2ReferenceOpenHashMap<>();
    private static MediaType mediaType = MediaType.parse("application/x-spark-sampler");
    private static ExecutorService executor = Executors.newSingleThreadScheduledExecutor((new ThreadFactoryBuilder()).setNameFormat("spark-loli-async-worker").build());

    public static void start(String key) {
        if (!ongoingSamplers.containsKey(key)) {
            Sampler sampler;
            try {
                AsyncProfilerAccess.INSTANCE.getProfiler();
                sampler = new AsyncSampler(4000, LoliConfig.instance.includeAllThreadsWhenProfiling ? ThreadDumper.ALL : new ThreadDumper.Specific(new long[] { Thread.currentThread().getId() } ), ThreadGrouper.BY_NAME);
            } catch (UnsupportedOperationException e) {
                sampler = new JavaSampler(4000, LoliConfig.instance.includeAllThreadsWhenProfiling ? ThreadDumper.ALL : new ThreadDumper.Specific(new long[] { Thread.currentThread().getId() } ), ThreadGrouper.BY_NAME, -1, !LoliConfig.instance.includeAllThreadsWhenProfiling, !LoliConfig.instance.includeAllThreadsWhenProfiling);
            }
            ongoingSamplers.put(key, sampler);
            LoliLogger.instance.warn("Profiler has started for stage [{}]...", key);
            sampler.start();
        }
    }

    public static void stop(String key) {
        Sampler sampler = ongoingSamplers.remove(key);
        if (sampler != null) {
            sampler.stop();
            output(key, sampler);
        }
    }

    private static void output(String key, Sampler sampler) {
        executor.execute(() -> {
            LoliLogger.instance.warn("The active profiler has been stopped! Uploading results...");
            byte[] output = sampler.formCompressedDataPayload(platformInfo, commandSender, ThreadNodeOrder.BY_TIME, "Stage: " + key, MergeMode.separateParentCalls(new MethodDisambiguator()));
            try {
                String urlKey = SparkPlatform.BYTEBIN_CLIENT.postContent(output, mediaType, false).key();
                String url = "https://spark.lucko.me/" + urlKey;
                LoliLogger.instance.warn("Profiler results for Stage [{}]: {}", key, url);
            } catch (Exception e) {
                LoliLogger.instance.fatal("An error occurred whilst uploading the results.", e);
            }
        });
    }

    static class LoliPlatformInfo extends AbstractPlatformInfo {

        @Override
        public Type getType() {
            return LoliLoadingPlugin.isClient ? Type.CLIENT : Type.SERVER;
        }

        @Override
        public String getName() {
            return "LoliASM";
        }

        @Override
        public String getVersion() {
            return LoliLoadingPlugin.VERSION;
        }

        @Override
        public String getMinecraftVersion() {
            return "1.12.2";
        }

    }

    public static class LoliCommandSender implements CommandSender {

        private final UUID uuid = UUID.randomUUID();
        private final String name;

        public LoliCommandSender() {
            this.name = "LoliASM";
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public UUID getUniqueId() {
            return uuid;
        }

        @Override
        public void sendMessage(Component component) { }

        @Override
        public boolean hasPermission(String s) {
            return true;
        }

    }

}

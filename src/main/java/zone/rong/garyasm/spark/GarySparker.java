package zone.rong.garyasm.spark;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import me.lucko.spark.common.SparkPlatform;
import me.lucko.spark.common.command.sender.CommandSender;
import me.lucko.spark.common.heapdump.HeapDumpSummary;
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
import zone.rong.garyasm.GaryLogger;
import zone.rong.garyasm.config.GaryConfig;
import zone.rong.garyasm.core.GaryLoadingPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GarySparker {

    private static PlatformInfo platformInfo = new GaryPlatformInfo();
    private static CommandSender commandSender = new GaryCommandSender();
    private static Map<String, Sampler> ongoingSamplers = new Object2ReferenceOpenHashMap<>();
    private static MediaType mediaType = MediaType.parse("application/x-spark-sampler");
    private static MediaType heapMediaType = MediaType.parse("application/x-spark-heap");
    private static ExecutorService executor = Executors.newSingleThreadScheduledExecutor((new ThreadFactoryBuilder()).setNameFormat("spark-gary-async-worker").build());

    public static void start(String key) {
        if (!ongoingSamplers.containsKey(key)) {
            Sampler sampler;
            try {
                AsyncProfilerAccess.INSTANCE.getProfiler();
                sampler = new AsyncSampler(4000, GaryConfig.instance.includeAllThreadsWhenProfiling ? ThreadDumper.ALL : new ThreadDumper.Specific(new long[] { Thread.currentThread().getId() } ), ThreadGrouper.BY_NAME);
            } catch (UnsupportedOperationException e) {
                sampler = new JavaSampler(4000, GaryConfig.instance.includeAllThreadsWhenProfiling ? ThreadDumper.ALL : new ThreadDumper.Specific(new long[] { Thread.currentThread().getId() } ), ThreadGrouper.BY_NAME, -1, !GaryConfig.instance.includeAllThreadsWhenProfiling, !GaryConfig.instance.includeAllThreadsWhenProfiling);
            }
            ongoingSamplers.put(key, sampler);
            GaryLogger.instance.warn("Profiler has started for stage [{}]...", key);
            sampler.start();
        }
    }

    public static void checkHeap(boolean summarize, boolean runGC) {
        if (runGC) {
            System.gc();
        }
        if (summarize) {
            executor.execute(() -> {
                byte[] output = HeapDumpSummary.createNew().formCompressedDataPayload(platformInfo, commandSender);
                try {
                    String urlKey = SparkPlatform.BYTEBIN_CLIENT.postContent(output, heapMediaType, false).key();
                    String url = "https://spark.lucko.me/" + urlKey;
                    GaryLogger.instance.warn("Heap Summary: {}", url);
                } catch (Exception e) {
                    GaryLogger.instance.fatal("An error occurred whilst uploading heap summary.", e);
                }
            });
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
            GaryLogger.instance.warn("Stage [{}] profiler has stopped! Uploading results...", key);
            byte[] output = sampler.formCompressedDataPayload(platformInfo, commandSender, ThreadNodeOrder.BY_TIME, "Stage: " + key, MergeMode.separateParentCalls(new MethodDisambiguator()));
            try {
                String urlKey = SparkPlatform.BYTEBIN_CLIENT.postContent(output, mediaType, false).key();
                String url = "https://spark.lucko.me/" + urlKey;
                GaryLogger.instance.warn("Profiler results for Stage [{}]: {}", key, url);
            } catch (Exception e) {
                GaryLogger.instance.fatal("An error occurred whilst uploading the results.", e);
            }
        });
    }

    static class GaryPlatformInfo extends AbstractPlatformInfo {

        @Override
        public Type getType() {
            return GaryLoadingPlugin.isClient ? Type.CLIENT : Type.SERVER;
        }

        @Override
        public String getName() {
            return "GaryASM";
        }

        @Override
        public String getVersion() {
            return GaryLoadingPlugin.VERSION;
        }

        @Override
        public String getMinecraftVersion() {
            return "1.12.2";
        }

    }

    public static class GaryCommandSender implements CommandSender {

        private final UUID uuid = UUID.randomUUID();
        private final String name;

        public GaryCommandSender() {
            this.name = "GaryASM";
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

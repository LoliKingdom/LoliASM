package zone.rong.loliasm.spark;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import me.lucko.spark.common.command.sender.CommandSender;
import me.lucko.spark.common.SparkPlatform;
import me.lucko.spark.common.SparkPlugin;
import me.lucko.spark.common.platform.AbstractPlatformInfo;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.sampler.ThreadDumper;
import me.lucko.spark.lib.adventure.text.Component;
import me.lucko.spark.lib.adventure.text.TextComponent;
import net.minecraft.launchwrapper.Launch;
import zone.rong.loliasm.LoliLogger;
import zone.rong.loliasm.core.LoliLoadingPlugin;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

public class LoliSparker implements SparkPlugin {

    private static final Map<String, LoliSparker> sparkers = new Object2ReferenceOpenHashMap<>();
    private static final PlatformInfo info = new LoliPlatformInfo();

    public static void start(String key) {
        if (sparkers.containsKey(key)) {
            return;
        }
        LoliSparker sparker = new LoliSparker();
        sparkers.put(key, sparker);
        sparker.platform.enable();
        sparker.platform.executeCommand(sparker.sender, new String[] { "sampler" });
    }

    public static void stop(String key) {
        LoliSparker sparker = sparkers.get(key);
        if (sparker != null) {
            // TODO more args?
            sparker.platform.executeCommand(sparker.sender, new String[] { "sampler", "--stop" });
            sparker.platform.disable();
            sparkers.remove(key);
        }
    }

    private final ScheduledExecutorService scheduler;
    private final SparkPlatform platform;
    private final CommandSender sender;

    public LoliSparker() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor((new ThreadFactoryBuilder()).setNameFormat("spark-loli-async-worker").build());
        this.platform = new SparkPlatform(this);
        this.sender = new LoliCommandSender();
    }

    @Override
    public String getVersion() {
        return LoliLoadingPlugin.VERSION;
    }

    @Override
    public Path getPluginDirectory() {
        return new File(Launch.minecraftHome, "config").toPath();
    }

    @Override
    public String getCommandName() {
        return "lolispark";
    }

    @Override
    public Stream<? extends CommandSender> getSendersWithPermission(String s) {
        return Stream.of(this.sender);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        this.scheduler.execute(runnable);
    }

    @Override
    public ThreadDumper getDefaultThreadDumper() {
        return new ThreadDumper.Specific(new long[]{ Thread.currentThread().getId() });
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return info;
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

        @Override
        public String getName() {
            return "LoliASM";
        }

        @Override
        public UUID getUniqueId() {
            return uuid;
        }

        @Override
        public void sendMessage(Component component) {
            if (component instanceof TextComponent) {
                String content = ((TextComponent) component).content();
                if (!content.trim().isEmpty()) {
                    LoliLogger.instance.warn(((TextComponent) component).content());
                }
            }

        }

        @Override
        public boolean hasPermission(String s) {
            return true;
        }

    }

    /*
    public static class LoliTicker implements TickCounter {

        private final Executor executor = Executors.newSingleThreadExecutor();

        private final Set<TickTask> tasks = Sets.newConcurrentHashSet();
        private final AtomicBoolean running = new AtomicBoolean(true);
        private final AtomicInteger ticks = new AtomicInteger(0);

        @Override
        public void start() {
            executor.execute(() -> {
                while (running.get()) {
                    if (System.currentTimeMillis() % 50 == 0) {
                        tasks.forEach(task -> task.onTick(LoliTicker.this));
                        ticks.incrementAndGet();
                    }
                }
            });
        }

        @Override
        public void close() {
            running.set(false);
        }

        @Override
        public int getCurrentTick() {
            return ticks.get();
        }

        @Override
        public void addTickTask(TickTask tickTask) {
            tasks.add(tickTask);
        }

        @Override
        public void removeTickTask(TickTask tickTask) {
            tasks.remove(tickTask);
        }
    }
     */

}

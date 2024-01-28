package zone.rong.blahajasm.api;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zone.rong.blahajasm.BlahajLogger;

import java.util.ArrayList;
import java.util.Locale;

public class LoliStringPool {

    public static final int FILE_PERMISSIONS_ID = 1;

    private static final Int2ObjectMap<Internal> POOLS = new Int2ObjectArrayMap<>();

    static {
        establishPool(-1, 12288, "", " ");
        POOLS.defaultReturnValue(POOLS.get(-1));
    }

    public static void establishPool(int poolId, int expectedSize, String... startingValues) {
        if (POOLS.containsKey(poolId)) {
            return;
        }
        POOLS.put(poolId, new Internal(poolId, expectedSize, startingValues));
    }

    public static Internal purgePool(int poolId) {
        return POOLS.remove(poolId);
    }

    public static int getSize() {
        return POOLS.defaultReturnValue().internalPool.size();
    }

    public static int getSize(int pool) {
        return POOLS.get(pool).internalPool.size();
    }

    public static long getDeduplicatedCount() {
        return POOLS.defaultReturnValue().deduplicatedCount;
    }

    public static long getDeduplicatedCount(int pool) {
        return POOLS.get(pool).deduplicatedCount;
    }

    public static String canonicalize(String string) {
        synchronized (POOLS) {
            return POOLS.defaultReturnValue().addOrGet(string);
        }
    }

    public static String unsafe$Canonicalize(String string) {
        return POOLS.defaultReturnValue().addOrGet(string);
    }

    @SuppressWarnings("unused")
    public static String lowerCaseAndCanonicalize(String string) {
        synchronized (POOLS) {
            return POOLS.defaultReturnValue().addOrGet(string.toLowerCase(Locale.ROOT));
        }
    }

    @SuppressWarnings("unused")
    public static String unsafe$LowerCaseAndCanonicalize(String string) {
        return POOLS.defaultReturnValue().addOrGet(string.toLowerCase(Locale.ROOT));
    }

    public static String canonicalize(String string, int poolId, boolean checkMainPool) {
        if (checkMainPool) {
            synchronized (POOLS) {
                String canonicalized = POOLS.get(poolId).internalPool.get(string);
                if (canonicalized != null) {
                    return canonicalized;
                }
            }
        }
        synchronized (POOLS) {
            return POOLS.get(poolId).addOrGet(string);
        }
    }

    public static String unsafe$Canonicalize(String string, int poolId, boolean checkMainPool) {
        if (checkMainPool) {
            String canonicalized = POOLS.get(poolId).internalPool.get(string);
            if (canonicalized != null) {
                return canonicalized;
            }
        }
        return POOLS.get(poolId).addOrGet(string);
    }

    @SubscribeEvent
    public static void onDebugList(RenderGameOverlayEvent.Text event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.gameSettings.showDebugInfo) {
            ArrayList<String> list = event.getLeft();
            if (!list.get(list.size() - 1).equals("")) {
                list.add("");
            }
            int size = getSize();
            long deduplicatedCount = getDeduplicatedCount();
            list.add(String.format("%s%s%s: %s strings processed. %s unique, %s deduplicated.", TextFormatting.AQUA, "<BlahajASM>", TextFormatting.RESET, deduplicatedCount, size, deduplicatedCount - size));
        }
    }

    static class Internal {

        final int id;
        final ObjectOpenHashSet<String> internalPool;

        long deduplicatedCount;

        @SuppressWarnings("all")
        Internal(int id, int expectedSize, String... startingValues) {
            this.id = id;
            this.internalPool = new ObjectOpenHashSet<>(expectedSize);
            for (String startingValue : startingValues) {
                this.internalPool.add(startingValue);
            }
        }

        String addOrGet(String string) {
            deduplicatedCount++;
            return internalPool.addOrGet(string);
        }

        @Override
        protected void finalize() {
            BlahajLogger.instance.warn("Clearing BlahajStringPool {}", id);
        }
    }

}

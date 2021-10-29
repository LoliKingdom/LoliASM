package zone.rong.loliasm.api;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Locale;

public class LoliStringPool {

    private static long deduplicatedCount = 0;

    private static final ObjectOpenHashSet<String> POOL = new ObjectOpenHashSet<>(12288);

    public static int getSize() {
        return POOL.size();
    }

    public static long getDeduplicatedCount() {
        return deduplicatedCount;
    }

    public static String canonicalize(String string) {
        synchronized (POOL) {
            deduplicatedCount++;
            return POOL.addOrGet(string);
        }
        // return string.length() == 0 ? "" : string.intern();
    }

    @SuppressWarnings("unused")
    public static String lowerCaseAndCanonicalize(String string) {
        synchronized (POOL) {
            deduplicatedCount++;
            return POOL.addOrGet(string.toLowerCase(Locale.ROOT));
        }
        // return string.length() == 0 ? "" : string.toLowerCase(Locale.ROOT).intern();
    }

    @SubscribeEvent
    public static void onDebugList(RenderGameOverlayEvent.Text event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.gameSettings.showDebugInfo) {
            ArrayList<String> list = event.getLeft();
            if (!list.get(list.size() - 1).equals("")) {
                list.add("");
            }
            event.getLeft().add(String.format("%s%s%s: %s strings processed. %s unique, %s deduplicated.", TextFormatting.AQUA, "<LoliASM>", TextFormatting.RESET, deduplicatedCount, LoliStringPool.getSize(), deduplicatedCount - LoliStringPool.getSize()));
        }
    }

}

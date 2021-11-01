package zone.rong.loliasm.bakedquad;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zone.rong.loliasm.LoliLogger;

import java.util.ArrayList;

public class LoliVertexDataPool {

    private static int deduplicatedCount = 0;
    private static int uniqueCount = 0;
    // private static int previousUniqueCount = 0;
    // private static boolean invalidated = false;

    private static ObjectOpenCustomHashSet<int[]> POOL = new ObjectOpenCustomHashSet<>(8192, IntArrays.HASH_STRATEGY);

    public static int getSize() {
        return uniqueCount;
        // return invalidated ? previousUniqueCount + POOL.size() : POOL.size();
    }

    public static int getDeduplicatedCount() {
        return deduplicatedCount;
    }

    public static int[] canonicalize(int[] vertexData) {
        if (POOL == null) {
            return vertexData;
        }
        synchronized (POOL) {
            deduplicatedCount++;
            return POOL.addOrGet(vertexData);
        }
    }

    public static int[] canonicalize(int[] vertexData, BakedQuad quad) {
        if (POOL == null) {
            return vertexData;
        }
        synchronized (POOL) {
            if (quad instanceof UnpackedBakedQuad) {
                return vertexData; // vertexData can be modified on piping UnpackedBakedQuads, hence no canonicalization
            }
            deduplicatedCount++;
            return POOL.addOrGet(vertexData);
        }
    }

    public static void invalidate() {
        uniqueCount = POOL.size();
        // previousUniqueCount += POOL.size();
        // POOL.clear();
        // POOL.trim();
        POOL = null;
        LoliLogger.instance.warn("Clearing LoliVertexDataPool");
    }

    @SubscribeEvent
    public static void onDebugList(RenderGameOverlayEvent.Text event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.gameSettings.showDebugInfo) {
            ArrayList<String> list = event.getLeft();
            list.add(String.format("%s%s%s: %s vertex data arrays processed. %s unique, %s deduplicated.", TextFormatting.AQUA, "<LoliASM>", TextFormatting.RESET, deduplicatedCount, uniqueCount, deduplicatedCount - uniqueCount));
        }
    }

}

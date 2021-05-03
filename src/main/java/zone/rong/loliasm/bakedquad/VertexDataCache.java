package zone.rong.loliasm.bakedquad;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;

@SuppressWarnings("unused")
public class VertexDataCache {

    private static final ObjectOpenCustomHashSet<int[]> KNOWN_VERTEX_DATA = new ObjectOpenCustomHashSet<>(4096, IntArrays.HASH_STRATEGY);

    public static int[] canonize(int[] vertexData) {
        return KNOWN_VERTEX_DATA.addOrGet(vertexData);
    }

}

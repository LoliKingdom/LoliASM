package zone.rong.garyasm.api.datastructures.canonical;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import zone.rong.garyasm.api.GaryStringPool;

import java.util.Map;

public class AutoCanonizingHashMap<K, V> extends Object2ObjectOpenHashMap<K, V> {

    public AutoCanonizingHashMap() {
        super();
    }

    public AutoCanonizingHashMap(Map<K, V> map) {
        super(map);
    }

    @Override
    public V put(K k, V v) {
        if (k instanceof String) {
            k = (K) GaryStringPool.canonicalize((String) k);
        }
        if (v instanceof String) {
            v = (V) GaryStringPool.canonicalize((String) v);
        }
        return super.put(k, v);
    }
}

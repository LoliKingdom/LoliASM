package zone.rong.loliasm.api.datastructures.canonical;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import zone.rong.loliasm.api.StringPool;

import java.util.Set;

// TODO: Hook Deduplicator's pools
public class AutoCanonizingSet<K> extends ObjectOpenHashSet<K> {

    public AutoCanonizingSet(Set<K> set) {
        super(set);
    }

    @Override
    public boolean add(K k) {
        if (k instanceof String) {
            k = (K) StringPool.canonicalize((String) k);
        }
        return super.add(k);
    }
}

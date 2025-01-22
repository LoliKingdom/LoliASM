package zone.rong.loliasm.api.datastructures;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import zone.rong.loliasm.api.datastructures.canonical.AutoCanonizingArrayMap;
import zone.rong.loliasm.api.datastructures.canonical.AutoCanonizingHashMap;
import zone.rong.loliasm.config.LoliConfig;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class TagMap<K, V> implements Map<K, V> {

    private Map<K, V> map;
    private final int threshold;

    public TagMap() {
        this(LoliConfig.instance.optimizeNBTTagCompoundMapThreshold, LoliConfig.instance.nbtBackingMapStringCanonicalization, LoliConfig.instance.optimizeNBTTagCompoundBackingMap);
    }

    public TagMap(int threshold, boolean canonicalizeString, boolean optimizeMap) {
        this.threshold = optimizeMap ? threshold : -1;
        if (canonicalizeString) this.map = optimizeMap ? new AutoCanonizingArrayMap<>() : new AutoCanonizingHashMap<>();
        else this.map = optimizeMap ? new Object2ObjectArrayMap<>() : new Object2ObjectOpenHashMap<>();
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.map.get(key);
    }

    @Override
    public V put(K key, V value) {
        if (this.size() == this.threshold) {
            this.map = this.map instanceof AutoCanonizingArrayMap ? new AutoCanonizingHashMap<>(this.map) : new Object2ObjectOpenHashMap<>(this.map);
        }
        return this.map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return this.map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        if (this.threshold != -1) {
            if (this.map instanceof AutoCanonizingHashMap) this.map = new AutoCanonizingArrayMap<>();
            else if (this.map instanceof Object2ObjectOpenHashMap) this.map = new Object2ObjectArrayMap<>();
            else this.map.clear();
        }
        else this.map.clear();
    }

    @Nonnull
    @Override
    public Set<K> keySet() {
        return this.map.keySet();
    }

    @Nonnull
    @Override
    public Collection<V> values() {
        return this.map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.map.entrySet();
    }
}

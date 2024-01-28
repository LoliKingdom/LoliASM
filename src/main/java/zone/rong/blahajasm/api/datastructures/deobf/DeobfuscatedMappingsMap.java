package zone.rong.loliasm.api.datastructures.deobf;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import zone.rong.loliasm.api.datastructures.DummyMap;
import zone.rong.loliasm.api.datastructures.canonical.AutoCanonizingArrayMap;
import zone.rong.loliasm.api.LoliStringPool;

import java.util.Map;

/**
 * This is a special Map structure that replaces the raw and cached mappings.
 *
 * - Anything class that is a Minecraft or MinecraftForge class passes the check and is placed in the map
 * - Anything that has its inner maps containing String values that matches any SRG mapping is placed in the map
 */
public class DeobfuscatedMappingsMap extends Object2ObjectOpenHashMap<String, Map<String, String>> {

    // TODO: Move to deduplicator
    private static final ObjectOpenHashSet<AutoCanonizingArrayMap<String, String>> innerMapCanonicalCache = new ObjectOpenHashSet<>();

    // Typed as this for the MethodHandle
    public static Map<String, Map<String, String>> of(Map<String, Map<String, String>> startingMap, boolean isField) {
        return new DeobfuscatedMappingsMap(startingMap, isField);
    }

    private final String prefix;

    DeobfuscatedMappingsMap(Map<String, Map<String, String>> startingMap, boolean isField) {
        super(startingMap);
        this.prefix = isField ? "field_" : "func_";
    }

    @Override
    public Map<String, String> put(String s, Map<String, String> innerMap) {
        if (s.indexOf('/') == -1 || s.startsWith("net/minecraft")) { // If it is a Minecraft or MinecraftForge class, we add to the map (short circuiting operation to not check innerMap strings)
            return super.put(LoliStringPool.canonicalize(s), innerMapCanonicalCache.addOrGet(new AutoCanonizingArrayMap<>(innerMap)));
        } else if (innerMap.isEmpty()) { // If there are no methods or fields mapped to 's' class, we return null and add nothing in the map
            return innerMap;
        } else if (innerMap.values().stream().anyMatch(string -> string.startsWith(prefix))) {
            return super.put(LoliStringPool.canonicalize(s), innerMapCanonicalCache.addOrGet(new AutoCanonizingArrayMap<>(innerMap))); // Check if any of the values start with 'func_' or 'field_', indicating that these 99% are mappings
        }
        /*
        for (Map.Entry<String, String> entry : innerMap.entrySet()) {
            // Check if any of the values start with 'func_' or 'field_', indicating that these 99% are mappings
            if (Stream.of("func_", "field_").anyMatch(entry.getValue()::startsWith)) {
                return super.put(s, innerMap);
            }
        }
         */
        // If all of this fails, return innerMap.
        return innerMap;
    }

    @Override
    public Map<String, String> get(Object k) {
        Map<String, String> get = super.get(k);
        return get == null ? DummyMap.of() : get; // Return DummyMap instead of Collections.emptyMap to not throw UnsupportedOperationException when Map#put
    }
}

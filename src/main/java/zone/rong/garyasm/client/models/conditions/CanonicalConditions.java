package zone.rong.garyasm.client.models.conditions;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.multipart.ICondition;
import org.apache.commons.lang3.tuple.Pair;
import zone.rong.garyasm.proxy.ClientProxy;

import java.util.*;

/**
 * Huge thanks to the original implementation from malte0811's FerriteCore: https://github.com/malte0811/FerriteCore
 *
 * Improvements:
 * - In 1.12, concurrency isn't needed; hence normal fastutils structures are used rather than ConcurrentMaps
 * - Arrays are used rather than Lists
 * - Also canonicalized TRUE/FALSE IConditions' inner predicates
 */
@SuppressWarnings({"unused", "Guava"})
public class CanonicalConditions {

    public static final ICondition TRUE = state -> Predicates.alwaysTrue();
    public static final ICondition FALSE = state -> Predicates.alwaysFalse();

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Object2ObjectOpenCustomHashMap<Predicate<IBlockState>[], Predicate<IBlockState>> OR_CACHE = new Object2ObjectOpenCustomHashMap<>(32, ObjectArrays.HASH_STRATEGY);
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final Object2ObjectOpenCustomHashMap<Predicate<IBlockState>[], Predicate<IBlockState>> AND_CACHE = new Object2ObjectOpenCustomHashMap<>(32, ObjectArrays.HASH_STRATEGY);

    private static final Object2ObjectOpenHashMap<Pair<IProperty<?>, Comparable<?>>, Predicate<IBlockState>> STATE_HAS_PROPERTY_CACHE = new Object2ObjectOpenHashMap<>(32);

    static {
        ClientProxy.refreshAfterModels.add(CanonicalConditions::clear);
    }

    public static void clear() {
        OR_CACHE.clear();
        OR_CACHE.trim();
        AND_CACHE.clear();
        AND_CACHE.trim();
        STATE_HAS_PROPERTY_CACHE.clear();
        STATE_HAS_PROPERTY_CACHE.trim();
    }

    public static Predicate<IBlockState> orCache(Iterable<? extends ICondition> conditions, BlockStateContainer stateContainer) {
        return OR_CACHE.computeIfAbsent(canonicalize(conditions, stateContainer), CanonicalConditions::orChain);
    }

    public static Predicate<IBlockState> orCache(Predicate<IBlockState>[] array) {
        return OR_CACHE.computeIfAbsent(array, CanonicalConditions::orChain);
    }

    public static Predicate<IBlockState> andCache(Iterable<? extends ICondition> conditions, BlockStateContainer stateContainer) {
        return AND_CACHE.computeIfAbsent(canonicalize(conditions, stateContainer), CanonicalConditions::andChain);
    }

    @SuppressWarnings({"unchecked", "UnstableApiUsage"})
    public static Predicate<IBlockState> propertyValueCache(BlockStateContainer stateContainer, String key, String value, Splitter splitter) {
        IProperty<?> property = stateContainer.getProperty(key);
        if (property == null) {
            throw new RuntimeException(String.format("Unknown property '%s' on '%s'", key, stateContainer.getBlock().toString()));
        } else {
            String valueNoInvert = value;
            boolean invert = !valueNoInvert.isEmpty() && valueNoInvert.charAt(0) == '!';
            if (invert) {
                valueNoInvert = valueNoInvert.substring(1);
            }
            List<String> matchedStates = splitter.splitToList(valueNoInvert);
            if (matchedStates.isEmpty()) {
                throw new RuntimeException(String.format("Empty value '%s' for property '%s' on '%s'", value, key, stateContainer.getBlock().toString()));
            } else {
                Predicate<IBlockState> isMatchedState;
                if (matchedStates.size() == 1) {
                    isMatchedState = makePropertyPredicate(stateContainer, property, valueNoInvert, key, value);
                } else {
                    isMatchedState = orCache(matchedStates.stream()
                            .map(subValue -> makePropertyPredicate(stateContainer, property, subValue, key, value))
                            .sorted(Comparator.comparingInt(Predicate::hashCode))
                            .toArray(Predicate[]::new));
                }
                return invert ? Predicates.not(isMatchedState) : isMatchedState;
            }
        }
    }

    private static <T> Predicate<T> orChain(Predicate<T>[] array) {
        return state -> {
            for (Predicate<T> predicate : array) {
                if (predicate.test(state)) {
                    return true;
                }
            }
            return false;
        };
    }

    private static <T> Predicate<T> andChain(Predicate<T>[] array) {
        return state -> {
            for (Predicate<T> predicate : array) {
                if (!predicate.test(state)) {
                    return false;
                }
            }
            return true;
        };
    }

    @SuppressWarnings("unchecked")
    private static Predicate<IBlockState>[] canonicalize(Iterable<? extends ICondition> conditions, BlockStateContainer stateContainer) {
        ArrayList<Predicate<IBlockState>> list = new ArrayList<>();
        for (ICondition cond : conditions) {
            list.add(cond.getPredicate(stateContainer));
        }
        Predicate<IBlockState>[] array = list.toArray(new Predicate[0]);
        Arrays.sort(array, Comparator.comparingInt(Predicate::hashCode));
        return array;
    }

    @SuppressWarnings("ConstantConditions")
    private static <T extends Comparable<T>> Predicate<IBlockState> makePropertyPredicate(BlockStateContainer container, IProperty<T> property, String subValue, String key, String value) {
        Optional<T> optional = property.parseValue(subValue);
        if (!optional.isPresent()) {
            throw new RuntimeException(String.format("Unknown value '%s' for property '%s' on '%s' in '%s'", subValue, key, container.getBlock().toString(), value));
        } else {
            return STATE_HAS_PROPERTY_CACHE.computeIfAbsent(Pair.of(property, optional.get()), p -> s -> s.getValue(p.getLeft()).equals(p.getRight()));
        }
    }
}

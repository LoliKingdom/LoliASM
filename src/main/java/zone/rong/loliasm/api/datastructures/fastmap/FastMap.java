package zone.rong.loliasm.api.datastructures.fastmap;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import zone.rong.loliasm.api.datastructures.fastmap.key.BinaryFastMapKey;
import zone.rong.loliasm.api.datastructures.fastmap.key.CompactFastMapKey;
import zone.rong.loliasm.api.datastructures.fastmap.key.FastMapKey;

import javax.annotation.Nullable;
import java.util.*;

public class FastMap {

    private final IProperty<?>[] props;
    private final FastMapKey<?>[] keys;
    private final IBlockState[] states;

    public FastMap(Collection<IProperty<?>> properties, Map<Map<IProperty<?>, Comparable<?>>, IBlockState> valuesMap, boolean compact) {
        int size = properties.size();
        List<FastMapKey<?>> keys = new ArrayList<>(size);
        List<IProperty<?>> props = new ArrayList<>(size);
        int factorUpTo = 1;
        for (IProperty<?> prop : properties) {
            props.add(prop);
            FastMapKey<?> nextKey;
            if (compact) {
                nextKey = new CompactFastMapKey<>(prop, factorUpTo);
            } else {
                nextKey = new BinaryFastMapKey<>(prop, factorUpTo);
            }
            keys.add(nextKey);
            factorUpTo *= nextKey.getFactorToNext();
        }
        this.props = props.toArray(new IProperty[0]);
        this.keys = keys.toArray(new FastMapKey[0]);
        List<IBlockState> valuesList = new ArrayList<>(factorUpTo);
        for (int i = 0; i < factorUpTo; ++i) {
            valuesList.add(null);
        }
        for (Map.Entry<Map<IProperty<?>, Comparable<?>>, IBlockState> state : valuesMap.entrySet()) {
            valuesList.set(getIndexOf(state.getKey()), state.getValue());
        }
        this.states = valuesList.toArray(new IBlockState[0]);
    }

    /**
     * Computes the value for a neighbor state
     *
     * @param oldIndex The original state index
     * @param prop     The property to be replaced
     * @param value    The new value of this property
     * @return The value corresponding to the specified neighbor, or null if value is not a valid value for prop
     */
    @Nullable
    public <T extends Comparable<T>> IBlockState with(int oldIndex, IProperty<T> prop, T value) {
        final FastMapKey<T> keyToChange = getKeyFor(prop);
        if (keyToChange == null) {
            return null;
        }
        int newIndex = keyToChange.replaceIn(oldIndex, value);
        if (newIndex < 0) {
            return null;
        }
        return states[newIndex];
    }

    /**
     * @return The map index corresponding to the given property-value assignment
     */
    public int getIndexOf(Map<IProperty<?>, Comparable<?>> state) {
        int id = 0;
        for (FastMapKey<?> k : keys) {
            id += k.toPartialMapIndex(state.get(k.getProperty()));
        }
        return id;
    }

    /**
     * Returns the value assigned to a property at a given map index
     *
     * @param stateIndex The map index for the assignment to check
     * @param property   The property to retrieve
     * @return The value of the property or null if the state if not present
     */
    @Nullable
    public <T extends Comparable<T>> T getValue(int stateIndex, IProperty<T> property) {
        final FastMapKey<T> propId = getKeyFor(property);
        if (propId == null) {
            return null;
        }
        return propId.getValue(stateIndex);
    }

    /**
     * Returns the given property and its value in the given state
     *
     * @param propertyIndex The index of the property to retrieve
     * @param stateIndex    The index of the state to use for the value
     */
    public Map.Entry<IProperty<?>, Comparable<?>> getEntry(int propertyIndex, int stateIndex) {
        FastMapKey<?> key = getKey(propertyIndex);
        return new AbstractMap.SimpleImmutableEntry<>(key.getProperty(), key.getValue(stateIndex));
    }

    /**
     * Same as {@link FastMap#with(int, IProperty, Comparable)}, but usable when the type of the value to set is not
     * correctly typed
     */
    public <T extends Comparable<T>> IBlockState withUnsafe(int globalTableIndex, IProperty<T> property, Object newV) {
        final FastMapKey<T> keyToChange = getKeyFor(property);
        if (keyToChange == null) {
            return null;
        }
        int newIndex = keyToChange.replaceIn(globalTableIndex, (T) newV);
        if (newIndex < 0) {
            return null;
        }
        return states[newIndex];
    }

    public int numProperties() {
        return keys.length;
    }

    <T extends Comparable<T>> FastMapKey<T> getKey(int keyIndex) {
        return (FastMapKey<T>) keys[keyIndex];
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> FastMapKey<T> getKeyFor(IProperty<T> prop) {
        int index = -1;
        for (int i = 0; i < props.length; i++) {
            IProperty<?> p = props[i];
            if (p.equals(prop)) {
                return (FastMapKey<T>) keys[i];
            }
        }
        return null;
    }
}

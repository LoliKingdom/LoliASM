package zone.rong.loliasm.api.datastructures.fastmap;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

/**
 * Provides a way of converting between values of a property and indices in [0, #values). Most properties are covered
 * by one of the (faster) specific implementations, all other properties use the {@link GenericIndexer}
 *
 * Rongmario: changed GenericIndexer to only back a single T[] array instead of a T[] and a Map<T, Integer>
 *     this has a speed penalty but not by much.
 */
public abstract class PropertyIndexer<T extends Comparable<T>> {

    private static final Map<IProperty<?>, PropertyIndexer<?>> KNOWN_INDEXERS = new Reference2ObjectOpenHashMap<>();

    private final IProperty<T> property;
    private final int numValues;

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> PropertyIndexer<T> makeIndexer(IProperty<T> prop) {
        PropertyIndexer<?> unchecked = KNOWN_INDEXERS.computeIfAbsent(prop, propInner -> {
            PropertyIndexer<?> result = null;
            if (propInner instanceof PropertyBool) {
                result = new BoolIndexer((PropertyBool) propInner);
            } else if (propInner instanceof PropertyInteger) {
                result = new IntIndexer((PropertyInteger) propInner);
            } else if (WeirdVanillaEnumFacingIndexer.isApplicable(propInner)) {
                result = new WeirdVanillaEnumFacingIndexer((IProperty<EnumFacing>) propInner);
            } else if (propInner instanceof PropertyEnum<?>) {
                result = new EnumIndexer<>((PropertyEnum<?>) propInner);
            }
            if (result == null || !result.isValid()) {
                return new GenericIndexer<>(propInner);
            } else {
                return result;
            }
        });
        return (PropertyIndexer<T>) unchecked;
    }

    PropertyIndexer(IProperty<T> property) {
        this.property = property;
        this.numValues = property.getAllowedValues().size();
    }

    public IProperty<T> getProperty() {
        return property;
    }

    public int numValues() {
        return numValues;
    }

    @Nullable
    public abstract T byIndex(int index);

    public abstract int toIndex(T value);

    /**
     * Checks if this indexer is valid, i.e. iterates over the correct set of values in the correct order
     */
    protected boolean isValid() {
        Collection<T> allowed = getProperty().getAllowedValues();
        int index = 0;
        for (T val : allowed) {
            if (toIndex(val) != index || !val.equals(byIndex(index))) {
                return false;
            }
            ++index;
        }
        return true;
    }

    private static class BoolIndexer extends PropertyIndexer<Boolean> {

        BoolIndexer(PropertyBool property) {
            super(property);
        }

        @Override
        @Nullable
        public Boolean byIndex(int index) {
            switch (index) {
                case 0:
                    return Boolean.TRUE;
                case 1:
                    return Boolean.FALSE;
                default:
                    return null;
            }
        }

        @Override
        public int toIndex(Boolean value) {
            return value ? 0 : 1;
        }
    }

    private static class IntIndexer extends PropertyIndexer<Integer> {
        private final int min;

        IntIndexer(PropertyInteger property) {
            super(property);
            this.min = property.getAllowedValues().stream().min(Comparator.naturalOrder()).orElse(0);
        }

        @Override
        @Nullable
        public Integer byIndex(int index) {
            if (index >= 0 && index < numValues()) {
                return index + min;
            } else {
                return null;
            }
        }

        @Override
        public int toIndex(Integer value) {
            return value - min;
        }
    }

    private static class EnumIndexer<E extends Enum<E> & IStringSerializable> extends PropertyIndexer<E> {

        private final int ordinalOffset;
        private final E[] enumValues;

        EnumIndexer(PropertyEnum<E> property) {
            super(property);
            this.ordinalOffset = property.getAllowedValues().stream().mapToInt(Enum::ordinal).min().orElse(0);
            this.enumValues = property.getValueClass().getEnumConstants();
        }

        @Override
        @Nullable
        public E byIndex(int index) {
            final int arrayIndex = index + ordinalOffset;
            if (arrayIndex < enumValues.length) {
                return enumValues[arrayIndex];
            } else {
                return null;
            }
        }

        @Override
        public int toIndex(E value) {
            return value.ordinal() - ordinalOffset;
        }
    }

    /**
     * This is a kind of hack for a vanilla quirk: BlockStateProperties.FACING (which is used everywhere) has the order
     * NORTH, EAST, SOUTH, WEST, UP, DOWN
     * instead of the "canonical" order given by the enum
     */
    private static class WeirdVanillaEnumFacingIndexer extends PropertyIndexer<EnumFacing> {

        private static final EnumFacing[] ORDER = EnumFacing.values();

        static boolean isApplicable(IProperty<?> prop) {
            Collection<?> values = prop.getAllowedValues();
            if (values.size() != ORDER.length) {
                return false;
            }
            return Arrays.equals(ORDER, values.toArray());
        }

        WeirdVanillaEnumFacingIndexer(IProperty<EnumFacing> prop) {
            super(prop);
            Preconditions.checkState(isValid());
        }

        @Override
        @Nullable
        public EnumFacing byIndex(int index) {
            if (index >= 0 && index < ORDER.length) {
                return ORDER[index];
            } else {
                return null;
            }
        }

        @Override
        public int toIndex(EnumFacing value) {
            return value.ordinal();
        }
    }

    private static class GenericIndexer<T extends Comparable<T>> extends PropertyIndexer<T> {

        private final T[] values;

        GenericIndexer(IProperty<T> property) {
            super(property);
            this.values = (T[]) property.getAllowedValues().toArray();
        }

        @Override
        @Nullable
        public T byIndex(int index) {
            return values[index];
        }

        @Override
        public int toIndex(T value) {
            for (int i = 0; i < values.length; i++) {
                T v = values[i];
                if (v.equals(value)) {
                    return i;
                }
            }
            return -1;
        }
    }
}
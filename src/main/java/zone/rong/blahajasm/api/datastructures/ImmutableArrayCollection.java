package zone.rong.blahajasm.api.datastructures;

import com.google.common.collect.Iterators;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Super slim ImmutableList structure
 */
public class ImmutableArrayCollection<T> implements Collection<T> {

    private final T[] array;

    public ImmutableArrayCollection(T[] array, boolean clone) {
        this.array = clone ? array.clone() : array;
    }

    @Override
    public int size() {
        return array.length;
    }

    @Override
    public boolean isEmpty() {
        return array.length == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            for (T t : array) {
                if (t == null) {
                    return true;
                }
            }
        } else {
            for (T t : array) {
                if (t.equals(o)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.forArray(this.array);
    }

    @Override
    public Object[] toArray() {
        return array.clone();
    }

    @Override
    public <T1> T1[] toArray(T1[] dst) {
        T[] src = this.array;
        if (dst.length < src.length) {
            return (T1[]) Arrays.copyOf(src, src.length, dst.getClass());
        }
        System.arraycopy(src, 0, dst, 0, src.length);
        if (dst.length > src.length) {
            dst[src.length] = null;
        }
        return dst;
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            for (T t : array) {
                if (!t.equals(o)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Object[]) {
            return Arrays.equals(array, (Object[]) o);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }
}

package zone.rong.blahajasm.api.datastructures;

import it.unimi.dsi.fastutil.objects.ObjectIterators;

import java.util.*;

public class DummyList<K> implements List<K> {

    private static final DummyList INSTANCE = new DummyList<>();

    @SuppressWarnings("unchecked")
    public static <K> List<K> of() {
        return (DummyList<K>) INSTANCE;
    }

    @Override
    public int size() { return 0; }

    @Override
    public boolean isEmpty() { return true; }

    @Override
    public boolean contains(Object o) { return false; }

    @Override
    public Iterator<K> iterator() { return Collections.emptyIterator(); }

    @Override
    public Object[] toArray() { return new Object[0]; }

    @Override
    public <T> T[] toArray(T[] a) { return (T[]) new Object[0]; }

    @Override
    public boolean add(K k) { return false; }

    @Override
    public boolean remove(Object o) { return false; }

    @Override
    public boolean containsAll(Collection<?> c) { return false; }

    @Override
    public boolean addAll(Collection<? extends K> c) { return false; }

    @Override
    public boolean addAll(int index, Collection<? extends K> c) { return false; }

    @Override
    public boolean removeAll(Collection<?> c) { return false; }

    @Override
    public boolean retainAll(Collection<?> c) { return false; }

    @Override
    public void clear() { }

    @Override
    public K get(int index) { return null; }

    @Override
    public K set(int index, K element) { return null; }

    @Override
    public void add(int index, K element) { }

    @Override
    public K remove(int index) { return null; }

    @Override
    public int indexOf(Object o) { return -1; }

    @Override
    public int lastIndexOf(Object o) { return -1; }

    @Override
    public ListIterator<K> listIterator() { return ObjectIterators.EMPTY_ITERATOR; }

    @Override
    public ListIterator<K> listIterator(int index) { return ObjectIterators.EMPTY_ITERATOR; }

    @Override
    public List<K> subList(int fromIndex, int toIndex) { return DummyList.of(); }

}

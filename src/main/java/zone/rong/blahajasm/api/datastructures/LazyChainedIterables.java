package zone.rong.loliasm.api.datastructures;

import java.util.Iterator;
import java.util.function.Supplier;

public class LazyChainedIterables<T> implements Iterable<T> {

    private final Supplier<Iterable<T>>[] iterables;

    @SafeVarargs
    public LazyChainedIterables(Supplier<Iterable<T>>... iterables) {
        this.iterables = iterables;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int index = 0;
            Iterator<T> currentIterator = iterables[index].get().iterator();
            @Override
            public boolean hasNext() {
                if (!currentIterator.hasNext()) {
                    if (++index == iterables.length) {
                        return false;
                    }
                    currentIterator = iterables[index].get().iterator();
                }
                return true;
            }
            @Override
            public T next() {
                return currentIterator.next();
            }
        };
    }

}

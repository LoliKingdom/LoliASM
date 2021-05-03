package com.google.common.collect;

import java.util.Map;
import java.util.function.IntFunction;

public class LoliIterator<K> extends UnmodifiableIterator<Map.Entry<K, Comparable<?>>>{

    private final IntFunction<Map.Entry<K, Comparable<?>>> getIth;
    private final int length;

    private int currentIndex;

    public LoliIterator(IntFunction<Map.Entry<K, Comparable<?>>> getIth, int length) {
        this.getIth = getIth;
        this.length = length;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < length;
    }

    @Override
    public Map.Entry<K, Comparable<?>> next() {
        Map.Entry<K, Comparable<?>> next = getIth.apply(currentIndex);
        ++currentIndex;
        return next;
    }
}
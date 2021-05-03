package com.google.common.collect;

import javax.annotation.Nullable;
import java.util.Map;

public class LoliEntrySet<K> extends ImmutableSet<Map.Entry<K, Comparable<?>>> {

    private final Object viewedState;

    public LoliEntrySet(Object viewedState) {
        this.viewedState = viewedState;
    }

    @Override
    public UnmodifiableIterator<Map.Entry<K, Comparable<?>>> iterator() {
        return new LoliIterator<>(i -> (Map.Entry<K, Comparable<?>>) LoliImmutableMap.stateAndIndex.getBy(viewedState, i), size());
    }

    @Override
    public int size() {
        return LoliImmutableMap.numProperties.applyAsInt(viewedState);
    }

    @Override
    public boolean contains(@Nullable Object key) {
        if (!(key instanceof Map.Entry)) {
            return false;
        }
        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) key;
        Object valueInMap = LoliImmutableMap.stateAndKey.getBy(viewedState, entry.getKey());
        return valueInMap != null && valueInMap.equals(entry.getValue());
    }

    @Override
    boolean isPartialView() {
        return false;
    }
}

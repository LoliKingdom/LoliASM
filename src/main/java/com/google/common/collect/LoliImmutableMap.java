package com.google.common.collect;

import zone.rong.loliasm.api.datastructures.fastmap.StateAndIndex;
import zone.rong.loliasm.api.datastructures.fastmap.StateAndKey;
import zone.rong.loliasm.api.datastructures.fastmap.FastMapStateHolder;

import javax.annotation.Nullable;
import java.util.function.ToIntFunction;

public class LoliImmutableMap<K> extends ImmutableMap<K, Comparable<?>> {

    public static ToIntFunction<Object> numProperties;
    public static StateAndKey stateAndKey;
    public static StateAndIndex stateAndIndex;

    private final FastMapStateHolder viewedState;

    public LoliImmutableMap(FastMapStateHolder viewedState) {
        this.viewedState = viewedState;
    }

    @Override
    public int size() {
        return numProperties.applyAsInt(viewedState);
    }

    @Override
    public Comparable<?> get(@Nullable Object key) {
        return stateAndKey.getBy(viewedState, key);
    }

    @Override
    public ImmutableSet<Entry<K, Comparable<?>>> createEntrySet() {
        return new LoliEntrySet<>(viewedState);
    }

    @Override
    public ImmutableSet<Entry<K, Comparable<?>>> entrySet() {
        return new LoliEntrySet<>(viewedState);
    }

    @Override
    boolean isPartialView() {
        return false;
    }
}

package io.github.isoyigido.bluff.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LoopingIterator<T> {
    public static <T> LoopingIterator<T> of(List<T> list) {
        return new LoopingIterator<>(list);
    }

    private final List<T> list;
    private final Map<T, Integer> map;

    private int index = 0;

    private LoopingIterator(List<T> list) {
        this.list = new ArrayList<>(list);
        this.map = new HashMap<>(list.size());
        for (int i = 0; i < this.list.size(); i++) {
            this.map.put(list.get(i), i);
        }
    }

    public T set(T object) {
        this.index = this.map.get(object);
        return object;
    }

    public T next() {
        this.index = (this.index + 1) % this.list.size();
        return this.list.get(this.index);
    }

    public int size() {
        return this.list.size();
    }
}
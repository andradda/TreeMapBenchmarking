package dk.itu.thesis.impl;

import dk.itu.thesis.api.ConcurrentTree;
import dk.itu.thesis.impl.brown.ConcurrentChromaticTreeMap;

public class ChromaticTreeAdapter<K extends Comparable<K>, V>
        implements ConcurrentTree<K, V, Void> {

    private final ConcurrentChromaticTreeMap<K, V> map;

    public ChromaticTreeAdapter() {
        this.map = new ConcurrentChromaticTreeMap<>();
    }

    @Override
    public boolean insert(K key, V value) {
        return map.putIfAbsent(key, value) == null;
    }

    @Override
    public boolean delete(K key) {
        return map.remove(key) != null;
    }

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public boolean update(K key, V value) {
        return map.put(key, value) != null; // insert or update, return true if key was already present
    }

    @Override
    public Void snapshot() {
        throw new UnsupportedOperationException(
                "Brown chromatic tree does not support snapshots"
        );
    }
}
package dk.itu.thesis.impl;

import dk.itu.thesis.api.ConcurrentTree;
import edu.stanford.ppl.concurrent.SnapTreeMap;
import dk.itu.thesis.api.RangeQueryableTree;

import java.util.Collections;
import java.util.NavigableMap;

public class SnapTreeAdapter<K extends Comparable<K>, V>
        implements ConcurrentTree<K, V, NavigableMap<K, V>>,
        RangeQueryableTree<K, V, NavigableMap<K, V>> {

    private final SnapTreeMap<K, V> map;

    public SnapTreeAdapter() {
        this.map = new SnapTreeMap<>();
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
    public NavigableMap<K, V> snapshot() {
        return Collections.unmodifiableNavigableMap(map.clone());
    }

    @Override
    public NavigableMap<K, V> rangeQuery(K lowerInclusive, K upperInclusive) {
        NavigableMap<K, V> snap = map.clone();
        return Collections.unmodifiableNavigableMap(
                snap.subMap(lowerInclusive, true, upperInclusive, true)
        );
    }
}
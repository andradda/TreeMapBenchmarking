package dk.itu.thesis.impl;

import dk.itu.thesis.api.ConcurrentTree;
import dk.itu.thesis.api.RangeQueryableTree;
import dk.itu.thesis.impl.vcas.VcasBatchChromaticMapGC;

public class VcasBatchChromaticAdapter<K extends Comparable<? super K>, V>
        implements ConcurrentTree<K, V, Object[]>,
        RangeQueryableTree<K, V, Object[]> {

    private final VcasBatchChromaticMapGC<K, V> map;

    public VcasBatchChromaticAdapter() {
        this.map = new VcasBatchChromaticMapGC<>();
    }

    public VcasBatchChromaticAdapter(int batchingDegree) {
        this.map = new VcasBatchChromaticMapGC<>(batchingDegree);
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
        // this didn't exist in the original code, but it seems like a natural extension to add an update method that returns true if the key was already present (i.e., an update rather than an insert)
        return map.put(key, value) != null;
    }

    @Override
    public Object[] snapshot() {
        // This performs a snapshot-backed full range scan.
        // Assumes Integer keys in your benchmark.
        @SuppressWarnings("unchecked")
        K min = (K) Integer.valueOf(Integer.MIN_VALUE);

        @SuppressWarnings("unchecked")
        K max = (K) Integer.valueOf(Integer.MAX_VALUE);

        return map.rangeScan(min, max);
    }

    public Object[] rangeQuery(K lo, K hi) {
        return map.rangeScan(lo, hi);
    }

    public Object[] multiSearch(K[] keys) {
        return map.multiSearch(keys);
    }

    public int size() {
        return map.size();
    }
}
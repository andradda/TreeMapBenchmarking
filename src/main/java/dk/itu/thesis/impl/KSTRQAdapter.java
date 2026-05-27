package dk.itu.thesis.impl;

import dk.itu.thesis.api.ConcurrentTree;
import dk.itu.thesis.api.RangeQueryableTree;
import dk.itu.thesis.impl.brown.LockFreeKSTRQ;

public class KSTRQAdapter<K extends Comparable<? super K>, V>
        implements ConcurrentTree<K, V, Object[]>,
                   RangeQueryableTree<K, V, Object[]> {

    private static final int DEFAULT_K = 64;

    private final LockFreeKSTRQ<K, V> map;

    public KSTRQAdapter() {
        this(DEFAULT_K);
    }

    public KSTRQAdapter(int k) {
        this.map = new LockFreeKSTRQ<>(k);
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
        return map.put(key, value) != null;
    }

    /**
     * KSTRQ does not provide an O(1) snapshot handle.
     * This method materializes a full-range atomic range query.
     */
    @Override
    public Object[] snapshot() {
        @SuppressWarnings("unchecked")
        K min = (K) Integer.valueOf(Integer.MIN_VALUE);

        @SuppressWarnings("unchecked")
        K max = (K) Integer.valueOf(Integer.MAX_VALUE);

        return map.subSet(min, max);
    }

    @Override
    public Object[] rangeQuery(K lowerInclusive, K upperInclusive) {
        return map.subSet(lowerInclusive, upperInclusive);
    }

    public int size() {
        return map.size();
    }
}
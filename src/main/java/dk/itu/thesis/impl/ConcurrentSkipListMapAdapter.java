package dk.itu.thesis.impl;

import dk.itu.thesis.api.ConcurrentTree;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class ConcurrentSkipListMapAdapter implements ConcurrentTree<Integer, Integer, Map<Integer, Integer>> {

    private final ConcurrentSkipListMap<Integer, Integer> map = new ConcurrentSkipListMap<>();

    @Override
    public boolean insert(Integer key, Integer value) {
        return map.putIfAbsent(key, value) == null;
    }

    @Override
    public boolean delete(Integer key) {
        return map.remove(key) != null;
    }

    @Override
    public Integer get(Integer key) {
        return map.get(key);
    }

    @Override
    public boolean update(Integer key, Integer value) {
        return map.replace(key, value) != null;
    }

    @Override
    public Map<Integer, Integer> snapshot() {
        return new ConcurrentSkipListMap<>(map);
    }
}
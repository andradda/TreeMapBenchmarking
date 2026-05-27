package dk.itu.thesis.api;

public interface ConcurrentTree<K, V, S> {
    boolean insert(K key, V value);
    boolean delete(K key);
    V get(K key);
    boolean update(K key, V value);
    S snapshot();
}

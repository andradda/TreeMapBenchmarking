package dk.itu.thesis.api;

/*
    *  An interface for a tree data structure that supports range queries.
*/
public interface RangeQueryableTree<K, V, R> {
    R rangeQuery(K lowerInclusive, K upperInclusive);
}
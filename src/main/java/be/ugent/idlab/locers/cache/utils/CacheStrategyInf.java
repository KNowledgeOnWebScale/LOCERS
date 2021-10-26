package be.ugent.idlab.locers.cache.utils;

import java.util.Map;

public interface CacheStrategyInf<K,V> {

    public void add(K key, V value);
    public V check(K key);
    public void reference(K key);
    public Map<K,V> getData();
}

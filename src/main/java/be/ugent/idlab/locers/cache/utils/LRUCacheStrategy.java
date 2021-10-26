package be.ugent.idlab.locers.cache.utils;

import java.util.*;

public class LRUCacheStrategy<K,V> implements CacheStrategyInf<K,V> {
    // stores cache item keys
    protected Deque<K> doublyQueue;

    // store references to the cache keys and values
    protected HashMap<K,V> itemMap;

    // maximum capacity of cache
    protected final int CACHE_SIZE;

    public LRUCacheStrategy(int size){
        this.CACHE_SIZE = size;
        doublyQueue = new LinkedList<>();
        itemMap = new HashMap<>();
    }
    public void add(K key, V value){
        if (!itemMap.containsKey(key)) {
            //remove oldest item
            if (doublyQueue.size() == CACHE_SIZE) {
                K last = doublyQueue.removeLast();
                itemMap.remove(last);
            }
            //add new reference
            itemMap.put(key,value);
        }
        else {
            //remove key so it can be marked as recently used
            doublyQueue.remove(key);
        }
        //mark item as most recent (top of queue)
        doublyQueue.push(key);

    }
    /* Refer the item within the LRU cache */
    public V check(K key) {
        if (!itemMap.containsKey(key)) {
            return null;
        }
        else {
            //mark key as recently used
            doublyQueue.remove(key);
            doublyQueue.push(key);
            return itemMap.get(key);
        }
    }

    @Override
    public void reference(K key) {
        if (itemMap.containsKey(key)){
            //remove key so it can be marked as recently used
            doublyQueue.remove(key);
            //mark item as most recent (top of queue)
            doublyQueue.push(key);
        }
    }

    @Override
    public Map<K, V> getData() {
        return itemMap;
    }

    // display contents of cache
    public void display() {
        Iterator<K> itr = doublyQueue.iterator();
        while (itr.hasNext()) {
            System.out.print(itr.next() + " ");
        }
    }
    public static void main(String[] args) {
        LRUCacheStrategy<Integer,String> cache = new LRUCacheStrategy<>(4);
        cache.add(1,"one");
        cache.add(2,"two");
        cache.add(3,"three");
        cache.add(4,"four");
        cache.add(1,"one");
        cache.add(2,"two");
        cache.add(2,"two");
        cache.add(2,"two");
        cache.add(5,"five");
        cache.display();
        System.out.println(cache.check(3));
    }
}

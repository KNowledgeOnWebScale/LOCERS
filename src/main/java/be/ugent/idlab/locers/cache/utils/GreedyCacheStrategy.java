package be.ugent.idlab.locers.cache.utils;

import java.util.*;

public class GreedyCacheStrategy<K,V> implements CacheStrategyInf<K,V> {

    // store references to the cache keys and values
    protected HashMap<K,V> itemMap;



    public GreedyCacheStrategy(){
        itemMap = new HashMap<>();
    }
    public void add(K key, V value){
        //add new reference
        itemMap.put(key,value);
    }
    /* Refer the item within the LRU cache */
    public V check(K key) {
        return itemMap.getOrDefault(key,null);
    }

    @Override
    public void reference(K key) {
        //greedy cache keeps all references
    }

    @Override
    public Map<K, V> getData() {
        return itemMap;
    }

    // display contents of cache
    public void display() {
        Iterator<K> itr = itemMap.keySet().iterator();
        while (itr.hasNext()) {
            System.out.print(itr.next() + " ");
        }
    }
    public static void main(String[] args) {
        GreedyCacheStrategy<Integer,String> cache = new GreedyCacheStrategy<>();
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

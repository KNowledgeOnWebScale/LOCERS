package be.ugent.idlab.locers.cache.utils;

public class FIFOCacheStrategy<K,V> extends LRUCacheStrategy<K,V> {


    public FIFOCacheStrategy(int size){
        super(size);
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
            doublyQueue.push(key);
        }
    }
    /* Refer the item within the LRU cache */
    public V check(K key) {
        if (!itemMap.containsKey(key)) {
            return null;
        }
        else {
            return itemMap.get(key);
        }
    }
    @Override
    public void reference(K key) {

    }

    public static void main(String[] args) {
        FIFOCacheStrategy<Integer,String> cache = new FIFOCacheStrategy<>(4);
        cache.add(1,"one");
        cache.add(2,"two");
        cache.add(3,"three");
        cache.add(4,"four");
        cache.add(1,"one");
       cache.add(2,"two");
        cache.add(2,"two");
//        cache.add(2,"two");
        cache.add(5,"five");
        cache.add(1,"one");
        cache.display();
        System.out.println(cache.check(3));
    }
}

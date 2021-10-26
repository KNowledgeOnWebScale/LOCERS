package be.ugent.idlab.locers.cache.utils;

public class MRUCacheStrategy<K,V> extends LRUCacheStrategy<K,V> {


    public MRUCacheStrategy(int size){
        super(size);
    }
    public void add(K key, V value) {
        if (!itemMap.containsKey(key)) {
            //remove newest item
            if (doublyQueue.size() == CACHE_SIZE) {
                K first = doublyQueue.removeFirst();
                itemMap.remove(first);
            }
            //add new reference
            itemMap.put(key, value);
        } else {
            //remove key so it can be marked as recently used
            doublyQueue.remove(key);
        }
        //mark item as most recent (top of queue)
        doublyQueue.push(key);

    }

    public static void main(String[] args) {
        MRUCacheStrategy<Integer,String> cache = new MRUCacheStrategy<>(4);
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

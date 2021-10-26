package be.ugent.idlab.locers.cache.utils;

public class LIFOCacheStrategy<K,V> extends FIFOCacheStrategy<K,V> {


    public LIFOCacheStrategy(int size){
        super(size);
    }
    public void add(K key, V value){
        if (!itemMap.containsKey(key)) {
            //remove newest item
            if (doublyQueue.size() == CACHE_SIZE) {
                K first = doublyQueue.removeFirst();
                itemMap.remove(first);
            }
            //add new reference
            itemMap.put(key,value);
            doublyQueue.push(key);
        }
    }


    public static void main(String[] args) {
        LIFOCacheStrategy<Integer,String> cache = new LIFOCacheStrategy<>(4);
        cache.add(1,"one");
        cache.add(2,"two");
        cache.add(3,"three");
        cache.add(4,"four");
//        cache.add(1,"one");
//       cache.add(2,"two");
//        cache.add(2,"two");
//        cache.add(2,"two");
        cache.add(5,"five");
        cache.add(2,"two");
        cache.add(6,"six");
        cache.display();
        System.out.println(cache.check(3));
    }
}

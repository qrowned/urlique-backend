package studio.urlique.api;

public interface Cacheable<K, V> {

    void invalidate(K key);

    void invalidateAll();

}

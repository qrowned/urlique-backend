package studio.urlique.api;

@Deprecated
public interface Cacheable<K, V> {

    void invalidate(K key);

    void invalidateAll();

}

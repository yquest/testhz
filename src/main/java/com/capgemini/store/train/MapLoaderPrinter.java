package com.capgemini.store.train;

import com.hazelcast.map.MapLoader;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

public class MapLoaderPrinter<K, V> {
    private final Class<? extends MapLoader<K, V>> loaderType;

    public MapLoaderPrinter(Class<? extends MapLoader<K, V>> loaderType) {
        this.loaderType = loaderType;
    }

    public void load(K obj) {
        System.out.printf("%s %s load %s %n", Instant.now(), loaderType, obj);
    }

    public void loadAll(Collection<K> obj) {
        System.out.printf("%s %s loadAll %s %n", Instant.now(), loaderType, obj);
    }

    public void loadAllKeys() {
        System.out.printf("%s %s loadAllKeys %n", Instant.now(), loaderType);
    }

    public void store(K key, V value) {
        System.out.printf("%s store key:%s value:%s%n", Instant.now(), key, value);
    }

    public void deleteKey(K key) {
        System.out.printf("%s delete key:%s%n", Instant.now(), key);
    }

    public void deleteKeys(Collection<K> keys) {
        System.out.println("delete map:");
        for (K key : keys) {
            System.out.printf("    %s delete keys:%s%n", Instant.now(), key);
        }
    }

    public void store(Map<K, V> map) {
        System.out.println("store map:");
        for (Map.Entry<K, V> entry : map.entrySet()) {
            System.out.printf("    %s store entry:%s %n", Instant.now(), entry);
        }
    }
}

package org.cdmckay.coffeep.examples;

import java.util.*;

public class TestSet implements Set<String> {

    private HashSet<String> set = new HashSet<String>();

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<String> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(String s) {
        return false;
    }

    public boolean addTwo(String s1, String s2) throws RuntimeException {
        return add(s1) && add(s2);
    }

    public <K, V> boolean addValues(Map<K, V> map, int limit) {
        boolean result = true;
        int count = 0;
        for (V value : map.values()) {
            if (count > limit) break;
            result = result && add(value.toString());
            count++;
        }
        return result;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {
    }

}
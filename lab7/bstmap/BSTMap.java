package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable, V> implements Map61B<K, V>{
    private Node root;
    private int size;

    private class Node {
        private K key;
        private V value;
        private Node left, right;

        private Node(){

        }

        private Node(K k, V v){
            key = k;
            value = v;
        }
    }

    public BSTMap(){
        root = null;
        size = 0;
    }

    @Override
    public void clear() {
        size = 0;
        root = null;
    }

    @Override
    public boolean containsKey(K key) {
        if(root == null){
            return false;
        }

        Node r = root;
        while (true){
            int cmp = key.compareTo(r.key);
            if(cmp == 0){
                return true;
            }else if(cmp > 0){
                if(r.right == null){
                    return false;
                }else{
                    r = r.right;
                }
            }else if(cmp < 0){
                if(r.left == null){
                    return false;
                }else{
                    r = r.left;
                }
            }
        }

    }

    @Override
    public V get(K key) {
        if(root == null){
            return null;
        }

        Node r = root;
        while (true){
            int cmp = key.compareTo(r.key);
            if(cmp == 0){
                return r.value;
            }else if(cmp > 0){
                if(r.right == null){
                    return null;
                }else{
                    r = r.right;
                }
            }else if(cmp < 0){
                if(r.left == null){
                    return null;
                }else{
                    r = r.left;
                }
            }
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if(root == null){
            root = new Node(key, value);
            size ++;
            return;
        }

        Node p = new Node(key, value);
        Node r = root;

        while (true){
            int cmp = p.key.compareTo(r.key);

            if(cmp > 0){
                if(r.right == null){
                    r.right = p;
                    size ++;
                    return;
                }else{
                    r = r.right;
                }
            }else if(cmp < 0){
                if(r.left == null){
                    r.left = p;
                    size ++;
                    return;
                }else{
                    r = r.left;
                }
            }else{
                return;
            }
        }

    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }

    public void printInOrder(Node n){
        if(n == null){
            return;
        }

        printInOrder(n.left);
        System.out.print(n.key);
        printInOrder(n.right);
    }
}

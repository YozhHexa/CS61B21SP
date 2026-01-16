package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private Node sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new Node();
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    private class Node {
        Node prev;
        Node next;
        T item;

        Node() {

        }

        Node(T i) {
            item = i;
        }
    }

    public int size() {
        return this.size;
    }

    public void addFirst(T item) {
        Node it = new Node(item);
        Node second = sentinel.next;

        sentinel.next = it;
        it.prev = sentinel;
        it.next = second;
        second.prev = it;

        size++;
    }

    public void addLast(T item) {
        Node it = new Node(item);
        Node secondToLast = sentinel.prev;

        sentinel.prev = it;
        it.next = sentinel;
        it.prev = secondToLast;
        secondToLast.next = it;

        size++;
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }

        Node fist = sentinel.next;
        T ans = fist.item;
        Node second = fist.next;

        sentinel.next = second;
        second.prev = sentinel;

        size--;
        return ans;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }

        Node last = sentinel.prev;
        T ans = last.item;
        Node lastToSecond = last.prev;

        sentinel.prev = lastToSecond;
        lastToSecond.next = sentinel;

        size--;
        return ans;
    }

    public T get(int index) {
        if (size < index) {
            return null;
        }

        Node cur = sentinel.next;
        for (int i = 0; i < index; i++) {
            cur = cur.next;
        }

        return cur.item;
    }

    public T getRecursive(int index) {
        if (size < index) {
            return null;
        }

        Node cur = sentinel.next;

        return getRecursiveHelper(cur, index);
    }

    private T getRecursiveHelper(Node n, int index) {
        if (index == 0) {
            return n.item;
        }

        return getRecursiveHelper(n.next, index - 1);
    }

    public Iterator<T> iterator() {
        return new LLDequeIterator();
    }

    private class LLDequeIterator implements Iterator<T> {
        Node p = sentinel.next;

        @Override
        public boolean hasNext() {
            return p != sentinel;
        }

        @Override
        public T next() {
            T item = p.item;
            p = p.next;
            return item;
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof Deque)) {
            return false;
        }

        if (((Deque<?>) o).size() != this.size()) {
            return false;
        }

        for (int i = 0; i < this.size(); i++) {
            if (!((Deque<?>) o).get(i).equals(this.get(i))) {
                return false;
            }
        }

        return true;
    }

    public void printDeque() {
        Node cur = sentinel.next;
        while (cur != sentinel) {
            System.out.print(String.valueOf(cur.item) + ' ');
            cur = cur.next;
        }
    }
}

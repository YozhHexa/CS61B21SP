package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T> {
    private T[] array;
    private int size;
    private int capacity;
    private int first;
    private int last;

    public ArrayDeque() {
        size = 0;
        capacity = 8;
        first = 0;
        last = capacity - 1;
        array = (T[]) new Object[capacity] ;
    }

    /**
     * change the ARRAY's capacity to C
     */
    private void resize(int c) {
        T[] a = (T[]) new Object[c];
        for (int i = 0; i < size; i++) {
            a[i] = array[(first + i) % capacity];
        }
        capacity = c;
        array = a;
        first = 0;
        last = size - 1;
    }


    public void addFirst(T item) {
        if (size == capacity) {
            resize(capacity * 2);
        }

        first = (first - 1 + capacity) % capacity;
        array[first] = item;
        size ++;
    }

    public void addLast(T item) {
        if (size == capacity) {
            resize(capacity * 2);
        }

        last = (last + 1) % capacity;
        array[last] = item;
        size ++;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return this.size;
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }

        if (size - 1 < capacity / 4 && capacity > 8) {
            resize(capacity / 2);
        }

        T ans = array[first];
        array[first] = null;
        first = (first + 1) % capacity;
        size--;

        return ans;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }

        if (size - 1 < capacity / 4 && capacity > 8) {
            resize(capacity / 2);
        }

        T ans = array[last];
        array[last] = null;
        last = (last - 1 + capacity) % capacity;
        size --;

        return ans;
    }

    public T get(int index) {
        if (size == 0) {
            return null;
        }

        return array[(index + first) % capacity];
    }
    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator{
        int cur = 0;
        /**
         */
        @Override
        public boolean hasNext() {
            return cur != size;
        }

        /**
         * Returns the next element in the iteration.
         */
        @Override
        public Object next() {
            T ans = array[(cur + first) % capacity];
            cur ++;
            return ans;
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof ArrayDeque)){
            return false;
        }

        if (((ArrayDeque<?>) o).size() != this.size()) {
            return false;
        }

        Iterator<?> it1 = ((ArrayDeque<?>) o).iterator();
        Iterator<T> it2 = this.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            Object a = it1.next();
            Object b = it2.next();
            if (!a.equals(b)) {
                return false;
            }
        }

        return true;
    }

    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.println(array[(i + first) % capacity].toString() + ' ');
        }
    }


}

package deque;

import java.util.Comparator;
import java.util.Iterator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> com;

    public MaxArrayDeque(Comparator<T> c) {
        com = c;
    }

    public T max() {
        if (com == null) {
            return null;
        }

        return max(com);
    }

    public T max(Comparator<T> c) {
        T m = null;

        Iterator<T> it = this.iterator();
        if (it.hasNext()) {
            m = it.next();
        }

        while (it.hasNext()) {
            T n = it.next();
            if (c.compare(n, m) > 0) {
                m = n;
            }
        }

        return m;
    }
}

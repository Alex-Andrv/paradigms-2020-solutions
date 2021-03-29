 package queue;

import java.util.Arrays;

public class ArrayQueue extends AbstractQueue{
    private final int DEFAULT_LENGTH = 10;
    private Object[] array = new Object[DEFAULT_LENGTH];
    private int lenArray = DEFAULT_LENGTH;
    private int first = 0;
    private int last = 0;

    //INV: size >= 0, array[first ... last) == {e1...en}

    // PRED: |E| == size
    // POST: E = {e[first]...e[first + 1], ... e[last - 1]}
    public Object[] toArray() {
        Object[] new_array = new Object[size];
        int count = last <= first && size != 0 ? lenArray - first : size;
        System.arraycopy(array, first, new_array, 0, count);
        if (last <= first && size != 0) {
            System.arraycopy(array, 0, new_array, count, last);
        }
        return new_array;
    }

    // PRED: data != null
    // POST: E = {e1, e2... en, data}
    public void enqueue(Object data) {
        if (size == lenArray) {
            array = Arrays.copyOf(toArray(), lenArray * 2);
            first = 0;
            last = size;
            lenArray *= 2;
        }
        array[last] = data;
        last = (last + 1) % lenArray;
        size++;
    }

    // PRED: |E| > 0
    // POST: R = e1
    public Object element() {
        return array[first];
    }

    // PRED: |E| > 0
    // POST: R = e1 && E = (e2..., en)
    public Object dequeue() {
        Object lastEl = array[first];
        array[first] = null;
        first = (first + 1) % lenArray;
        size--;
        return lastEl;
    }

    // PRED: true
    // POST: |E| = 0
    public void clear() {
        array = new Object[DEFAULT_LENGTH];
        first = 0;
        last = 0;
        size = 0;
    }
}

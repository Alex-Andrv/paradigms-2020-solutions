package queue;

public abstract class AbstractQueue implements Queue {
    protected int size = 0;

    // PRED: true
    // POST: R = |E|
    public int size() {
        return size;
    }

    // PRED: true
    // POST: R = (|E| == 0)
    public boolean isEmpty() {
        return (size == 0);
    }



}

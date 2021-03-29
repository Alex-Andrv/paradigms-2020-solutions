package queue;

public interface Queue {
    public Object[] toArray();
    public void enqueue(Object data);
    public Object element();
    public Object dequeue();
    public int size();
    public boolean isEmpty();
    public void clear();
}

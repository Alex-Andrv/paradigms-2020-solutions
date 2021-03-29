  package queue;

import java.util.Arrays;

public class LinkedQueue extends AbstractQueue{

    //INV: size >= 0, array[first ... last) == {e1...en}

    private class Node {
        public Object element;
        public Node next;
        public Node(Object element, Node next) {
            this.element = element;
            this.next = next;
        }
    }

    private Node head = null;
    private Node tail = null;

    // PRED: |E| == size
    // POST: res = |E|
    public Object[] toArray() {
        Object[] res = new Object[size];
        Node node  = head;
        for (int i = 0; i < size; i++) {
            res[i] = node.element;
            node = node.next;
        }
        return res;
    }

    // PRED: data != null
    // POST: E = {e1, e2... en, data}
    public void enqueue(Object data) {
        if (size == 0) {
            head = new Node(data, null);
            tail = head;
            size = 1;
        } else {
            tail.next = new Node(data, null);
            tail = tail.next;
            size++;
        }
    }

    // PRED: |E| > 0
    // POST: R = e1
    public Object element() {
        return head.element;
    }

    // PRED: |E| > 0
    // POST: R = e1 && E = (e2..., en)
    public Object dequeue() {
        Object res = head.element;
        head = head.next;
        size--;
        return res;
    }


    // PRED: true
    // POST: |E| = 0
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }
}

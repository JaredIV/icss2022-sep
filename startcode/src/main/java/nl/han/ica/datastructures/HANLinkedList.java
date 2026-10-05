package nl.han.ica.datastructures;

public class HANLinkedList<T> implements IHANLinkedList<T>{
    LinkedElement<T> head;
    LinkedElement<T> tail;

    public HANLinkedList(T head){
        LinkedElement<T> start = new LinkedElement<>(head);
        this.head = this.tail = start;
    }
    public HANLinkedList(LinkedElement<T> head) {
        this.head = this.tail = head;
    }

    public HANLinkedList(LinkedElement<T> head, LinkedElement<T> tail) {
        this.head = head;
        this.tail = tail;
    }
    @Override
    public T getFirst() {
        return head.value;
    }


    public LinkedElement<T> getTail() {
        return tail;
    }

    @Override
    public void addLast(T tail) {
        LinkedElement<T> newTail = new LinkedElement<>(tail);
        if (this.tail == null){
            this.head = this.tail = newTail;
            return;
        }
        this.tail.setNextElement(newTail);
        this.tail = newTail;
    }

    @Override
    public void addFirst(T head) {
        LinkedElement<T> newHead = new LinkedElement<>(head);
        newHead.setNextElement(this.head);
        this.head = newHead;
    }
    @Override
    public void removeFirst(){
        if (this.head != this.tail) {
            this.head = this.head.nextElement;
        }
    }

    public void removeLast(){
        if (this.head == null) {
            return;
        }

        if (this.head == this.tail) {
            this.head = null;
            this.tail = null;
            return;
        }
        else {
            LinkedElement<T> secondLast = this.head;
            while (secondLast.nextElement.nextElement != null) {
                secondLast = secondLast.nextElement;
            }
            secondLast.nextElement = null;
            this.tail = secondLast;
        }
    }

    //orginale was dit get het elment voor opdracht moet het get value zijn
    @Override
    public T get(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }

        LinkedElement<T> current = this.head;

        for (int i = 0; i < index; i++) {
            if (current == null || current.nextElement == null) {
                throw new IndexOutOfBoundsException("Index out of bounds");
            }

            current = current.nextElement;
        }

        return current.value;
    }

    public LinkedElement<T> getElement(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }

        LinkedElement<T> current = this.head;

        for (int i = 0; i < index; i++) {
            if (current == null || current.nextElement == null) {
                throw new IndexOutOfBoundsException("Index out of bounds");
            }

            current = current.nextElement;
        }

        return current;
    }

    public HANLinkedList<T> slice(int start, int end) {
        if (start < 0 || end < 0 || start >= end) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }

        LinkedElement<T> beforeStart = null;

        if (start > 0) {
            beforeStart = getElement(start - 1);
        }

        LinkedElement<T> sliceHead = getElement(start);

        LinkedElement<T> sliceTail = getElement(end - 1);

        LinkedElement<T> afterEnd = sliceTail.nextElement;

        sliceTail.nextElement = null;

        if (beforeStart == null) {
            this.head = afterEnd;
        } else {
            beforeStart.nextElement = afterEnd;
        }

        if (afterEnd == null) {
            if (beforeStart == null) {
                this.head = null;
                this.tail = null;
            } else {
                this.tail = beforeStart;
            }
        }

        if (this.head == null) {
            this.tail = null;
        }

        return new HANLinkedList<>(sliceHead, sliceTail);
    }

    //nieuwe dingen die voor de IHANLinkedList zijn
    /**
     * Clears list. Size equals 0 afterwards
     */
    @Override
    public void clear(){
        this.head = null;
        this.tail = null;
    }

    /**
     * Adds value to index position
     * @param index the position
     * @param value the value to add at index
     */
    @Override
    public void insert(int index, T value) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }

        if (this.head == null) {
            if (index == 0) {
                LinkedElement<T> newElement = new LinkedElement<>(value);
                this.head = newElement;
                this.tail = newElement;
                return;
            }
            throw new IndexOutOfBoundsException("Index out of bounds");
        }


        if (index == 0) {
            this.head.value = value;
            return;
        }

        LinkedElement<T> current = this.head;

        for (int i = 0; i < index; i++) {
            if (current == null || current.nextElement == null) {
                throw new IndexOutOfBoundsException("Index out of bounds");
            }

            current = current.nextElement;
        }

        current.value = value;
    }

    /**
     * Deletes value at position
     * @param pos position where value is deleted
     */
    @Override
    public void delete(int pos){
        if (pos < 0) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }

        if (pos == 0) {
            this.head.value = null;
            return;
        }

        LinkedElement<T> current = this.head;

        for (int i = 0; i < pos; i++) {
            if (current == null || current.nextElement == null) {
                throw new IndexOutOfBoundsException("Index out of bounds");
            }

            current = current.nextElement;
        }

        current.value = null;
    }

    /**
     * Determines size of the list, equals the number of stored items but not the header node
     * @return number of items in list
     */
    @Override
    public int getSize() {
        int size = 0;
        LinkedElement<T> current = this.head;

        while (current != null) {
            size++;
            current = current.nextElement;
        }

        return size;
    }

}

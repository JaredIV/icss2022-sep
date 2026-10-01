package nl.han.ica.datastructures;

public class LinkedElement<T> {
    T value;
    LinkedElement<T> nextElement;

    public LinkedElement(T value) {
        this.value = value;
        this.nextElement = null;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public void setNextElement(LinkedElement<T> nextElement) {
        this.nextElement = nextElement;
    }

    public T getValue(){
        return value;
    }

    public LinkedElement<T> getNext(){
        return nextElement;
    }
}
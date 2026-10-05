package nl.han.ica.datastructures;

public class HANStack<T> implements IHANStack<T>{
    HANLinkedList<T> Stack;

    public HANStack(T x){
        this.Stack = new HANLinkedList<>(x);
    }

    public HANStack(){}

    @Override
    public void push(T x){
        if (this.Stack == null){
            this.Stack = new HANLinkedList<>(x);
        }
        this.Stack.addLast(x);
    }

    @Override
    public T pop(){
        T valeu = Stack.getTail().getValue();
        this.Stack.removeLast();
        return valeu;
    }

    @Override
    public T peek(){
        return Stack.getTail().getValue();
    }

//    @Override
//    public boolean isEmpty() {
//        return Stack.getHead() == null;
//    }

}

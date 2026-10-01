package nl.han.ica.datastructures;

public class Stack<T> implements IHANStack<T>{
    LinkedList<T> Stack;

    public Stack(T x){
        this.Stack = new LinkedList<>(x);
    }

    @Override
    public void push(T x){
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

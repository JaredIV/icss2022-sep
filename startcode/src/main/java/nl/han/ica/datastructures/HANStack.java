package nl.han.ica.datastructures;

public class HANStack<T> implements IHANStack<T>{
    HANLinkedList<T> Stack;

    public HANStack(T x){
        this.Stack = new HANLinkedList<>(x);
    }

    public HANStack(){}

    /**
     * pushes value T to the top of the stack
     * @param value value to push
     */
    @Override
    public void push(T x){
        if (this.Stack == null){
            this.Stack = new HANLinkedList<>(x);
        }
        this.Stack.addLast(x);
    }

    /**
     * Pops (and removes) value at top of stack
     * @return popped value
     */
    @Override
    public T pop(){
        T valeu = Stack.getTail().getValue();
        this.Stack.removeLast();
        return valeu;
    }

    /**
     * Peeks at the top of the stack. Does not remove anything
     * @return value at the top of the stack
     */
    @Override
    public T peek(){
        return Stack.getTail().getValue();
    }

//    @Override
//    public boolean isEmpty() {
//        return Stack.getHead() == null;
//    }

}

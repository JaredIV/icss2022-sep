package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues = new HANLinkedList<>();

        variableValues.addFirst(new HashMap<>());

        for (ASTNode node : ast.root.body) {
            tarnsform(node);
        }

    }

    private void tarnsform(ASTNode node) {

        if (node instanceof VariableAssignment){
            VariableAssignment assignment = (VariableAssignment) node;

            String name = assignment.name.name;
            Literal value = (Literal) assignment.expression;

            if (value != null){
                variableValues.getFirst().put(name, value);
            }
        }

        if (node instanceof IfClause){
            System.out.println(node);
            transformIfClause(node);
        }

        for (ASTNode child : node.getChildren()) {
            tarnsform(child);
        }
    }

    private void transformIfClause(ASTNode node) {
        IfClause ifClause = (IfClause) node;
        Expression condition = ifClause.getConditionalExpression();

        boolean result = false;

        if (condition instanceof VariableReference) {
            VariableReference reference = (VariableReference) condition;
            String name = reference.name;

            Literal value = variableValues.getFirst().get(name);

            if (value instanceof BoolLiteral) {
                result = ((BoolLiteral) value).value;
            }
        } else if (condition instanceof BoolLiteral) {
            BoolLiteral value = (BoolLiteral) condition;
            result = value.value;
        }

        if (result){
            ArrayList<ASTNode> Test = ((IfClause) node).body;
            System.out.println(Test);
        }else{
            ArrayList<ASTNode> Test = ((IfClause) node).getElseClause().body;
            System.out.println(Test);
        }

    }

}

package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;
import nl.han.ica.icss.ast.literals.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Checker {

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        ArrayList<ASTNode> body = ast.root.body;

        HashMap<String, ExpressionType> types = new HashMap<>();
        for (ASTNode node : body){
            if (node instanceof VariableAssignment) {
                VariableAssignment assignment = (VariableAssignment) node;

                String name = assignment.name.name;
                ExpressionType type = getExpressionType(assignment.expression);

                if (type != null) {
                    types.put(name, type);
                } else {
                    node.setError("Node " + name + " has an undefined type");
                }

            }
        }
        variableTypes.addFirst(types);
        for (ASTNode node : body){
            if (node instanceof Stylerule){
                System.out.println(node);
                checkUndifendVariables(node);
            }
        }

    }

    private ExpressionType getExpressionType(Expression expression) {
        if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        }
        return null;
    }

    private void checkUndifendVariables(ASTNode node) {
        if (node instanceof VariableReference) {
            VariableReference reference = (VariableReference) node;
            String name = reference.name;
            if (!variableTypes.getFirst().containsKey(name)) {
                node.setError("Variable " + name + " is undefined");
            }
        }

        for (ASTNode child : node.getChildren()) {
            checkUndifendVariables(child);
        }
    }

    private void checkOperationTypes(ASTNode node) {
        if (node instanceof AddOperation || node instanceof SubtractOperation) {
            checkAdditionAndSubtraction((Operation) node);
        } else if (node instanceof MultiplyOperation) {
            checkMultiplication((Operation) node);
        }

        for (ASTNode child : node.getChildren()) {
            checkOperationTypes(child);
        }
    }

    private void checkAdditionAndSubtraction(Operation operation) {

    }

    private void checkMultiplication(Operation operation) {

    }


}

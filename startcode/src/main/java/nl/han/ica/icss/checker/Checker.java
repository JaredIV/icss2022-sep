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

        HashMap<String, ExpressionType> globaalTypes = new HashMap<>();
        for (ASTNode node : body){
            if (node instanceof VariableAssignment) {
                VariableAssignment assignment = (VariableAssignment) node;

                String name = assignment.name.name;
                ExpressionType type = getExpressionType(assignment.expression);

                if (type != null) {
                    globaalTypes.put(name, type);
                } else {
                    node.setError("Node " + name + " has an undefined type");
                }

            }
        }
        variableTypes.addFirst(globaalTypes);
        for (ASTNode node : body) {
            if (node instanceof Stylerule) {
                startscope(node);
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
        }else if (expression instanceof VariableReference) {
            VariableReference reference = (VariableReference) expression;

            for (int i = 0; i < variableTypes.getSize(); i++) {
                HashMap<String, ExpressionType> scope = variableTypes.get(i);

                if (scope.containsKey(reference.name)) {
                    return scope.get(reference.name);
                }
            }
        }else if (expression instanceof MultiplyOperation) {
            Operation operation = (Operation) expression;

            ExpressionType leftType = getExpressionType(operation.lhs);
            ExpressionType rightType = getExpressionType(operation.rhs);

            if (leftType == ExpressionType.SCALAR) {
                return rightType;
            } else if (rightType == ExpressionType.SCALAR) {
                return leftType;
            }
        } else if (expression instanceof AddOperation || expression instanceof SubtractOperation) {
            Operation operation = (Operation) expression;

            ExpressionType leftType = getExpressionType(operation.lhs);
            ExpressionType rightType = getExpressionType(operation.rhs);

            if (leftType != null && leftType == rightType) {
                return leftType;
            }
        }
        return null;
    }

    private void startscope(ASTNode node) {
        HashMap<String, ExpressionType> localTypes = new HashMap<>();
        variableTypes.addFirst(localTypes);

        lokaalVaribels(node);
        
        checkOperationTypes(node);
        checkCorrectDeclertionType(node);
        checkIfstatments(node);

        variableTypes.removeFirst();
    }

    private void lokaalVaribels(ASTNode node) {
        if (node instanceof VariableAssignment) {
            VariableAssignment assignment = (VariableAssignment) node;

            String name = assignment.name.name;
            ExpressionType type = getExpressionType(assignment.expression);

            if (type != null) {
                variableTypes.getFirst().put(name, type);
            } else {
                node.setError("Node " + name + " has an undefined type");
            }
        } else if (node instanceof VariableReference) {
            checkUndifendVariables(node);
        }

        for (ASTNode child : node.getChildren()) {
            lokaalVaribels(child);
        }
    }

    private void checkUndifendVariables(ASTNode node) {
        if (node instanceof VariableReference) {
            VariableReference reference = (VariableReference) node;
            String name = reference.name;
            boolean found = false;

            for (int i = 0; i < variableTypes.getSize(); i++) {
                HashMap<String, ExpressionType> scope = variableTypes.get(i);

                if (scope.containsKey(name)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                node.setError("Variable " + name + " is undefined");
            }
        }

        for (ASTNode child : node.getChildren()) {
            checkUndifendVariables(child);
        }
    }

    private void checkOperationTypes(ASTNode node) {
        if (node instanceof AddOperation || node instanceof SubtractOperation) {
            checkAdditionAndSubtraction((Operation) node,node);
        } else if (node instanceof MultiplyOperation) {
            checkMultiplication((Operation) node,node);
        }

        for (ASTNode child : node.getChildren()) {
            checkOperationTypes(child);
        }
    }

    private void checkAdditionAndSubtraction(Operation operation,ASTNode node) {
        ExpressionType leftType = getExpressionType(operation.lhs);
        ExpressionType rightType = getExpressionType(operation.rhs);
        if (leftType != null && rightType != null && leftType != rightType) {
            node.setError("Operands of addition or subtraction must have the same type");
        }
        if (leftType == ExpressionType.BOOL || leftType == ExpressionType.COLOR || rightType == ExpressionType.BOOL || rightType == ExpressionType.COLOR) {
            node.setError("Operands of addition or subtraction not have type bool or color");
        }
    }

    private void checkMultiplication(Operation operation,ASTNode node) {
        ExpressionType leftType = getExpressionType(operation.lhs);
        ExpressionType rightType = getExpressionType(operation.rhs);
        if (leftType != null && rightType != null && leftType != ExpressionType.SCALAR && rightType != ExpressionType.SCALAR){
            node.setError("At least one operand of multiplication must be a scalar");
        }
        if (leftType == ExpressionType.BOOL || leftType == ExpressionType.COLOR || rightType == ExpressionType.BOOL || rightType == ExpressionType.COLOR) {
            node.setError("Operands of multiplication not have type bool or color");
        }

    }

    private void checkCorrectDeclertionType(ASTNode node) {
        if (node instanceof Declaration) {
            Declaration declaration = (Declaration) node;

            String propertyName = declaration.property.toString();
            ExpressionType type = getExpressionType(declaration.expression);

            if ((propertyName.contains("width") || propertyName.contains("height"))
                    && type != ExpressionType.PIXEL
                    && type != ExpressionType.PERCENTAGE) {
                node.setError("Property "+propertyName +" must have type PIXEL or PERCENTAGE");
            }

            if (propertyName.contains("color") && type != ExpressionType.COLOR) {
                node.setError("Property "+propertyName +" must have type COLOR");
            }
        }

        for (ASTNode child : node.getChildren()){
            checkCorrectDeclertionType(child);
        }
    }

    private void checkIfstatments(ASTNode node){
        if (node instanceof IfClause){
            IfClause ifClause = (IfClause) node;

            Expression condition = ifClause.getConditionalExpression();
            ExpressionType type = getExpressionType(condition);
            if (type != ExpressionType.BOOL){
                node.setError("IfClause must have type BOOL");
            }
        }

        for (ASTNode child : node.getChildren()){
            checkIfstatments(child);
        }
    }

}

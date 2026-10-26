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
        System.out.println(ast);
        ast.root.body = transformStatements(ast.root.body);
        System.out.println(ast);
    }

    private ArrayList<ASTNode> transformStatements(ArrayList<ASTNode> statements) {

        ArrayList<ASTNode> result = new ArrayList<>();

        for (ASTNode node : statements) {

            if (node instanceof VariableAssignment) {
                VariableAssignment assignment = (VariableAssignment) node;

                if (assignment.expression instanceof Literal) {
                    variableValues.getFirst().put(assignment.name.name, (Literal) assignment.expression);
                }

                continue;
            }

            if (node instanceof Stylerule) {
                Stylerule rule = (Stylerule) node;
                rule.body = transformStatements(rule.body);
                result.add(rule);
                continue;
            }

            if (node instanceof IfClause) {
                IfClause ifClause = (IfClause) node;

                boolean condition = evaluateCondition(
                        ifClause.getConditionalExpression()
                );

                ArrayList<ASTNode> selectedBody;

                if (condition) {
                    selectedBody = ifClause.body;
                } else if (ifClause.getElseClause() != null) {
                    selectedBody = ifClause.getElseClause().body;
                } else {
                    continue;
                }

                result.addAll(transformStatements(selectedBody));
                continue;
            }

            //alleen nu voor de berekeningen

            transformChildren(node);
            result.add(node);
        }

        return result;
    }

    private boolean evaluateCondition(Expression expression) {
        if (expression instanceof BoolLiteral) {
            return ((BoolLiteral) expression).value;
        }

        if (expression instanceof VariableReference) {
            VariableReference reference = (VariableReference) expression;

            Literal value = variableValues.getFirst().get(reference.name);

            if (value instanceof BoolLiteral) {
                return ((BoolLiteral) value).value;
            }
        }

        return false;
    }

    private void transformChildren(ASTNode node) {
        ArrayList<ASTNode> children = new ArrayList<>(node.getChildren());

        for (ASTNode child : children) {
            if (child instanceof VariableReference) {
                VariableReference reference = (VariableReference) child;

                Literal value =
                        variableValues.getFirst().get(reference.name);

                if (value != null) {
                    node.removeChild(child);
                    node.addChild(value);
                }
            } else {
                transformChildren(child);
            }
        }
    }
}
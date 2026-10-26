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
        ast.root.body = transformStatements(ast.root.body);
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

        if (node instanceof Declaration) {
            Declaration declaration = (Declaration) node;

            if (declaration.expression != null) {
                declaration.expression = transformExpression(declaration.expression);
            }

            return;
        }

        ArrayList<ASTNode> children =
                new ArrayList<>(node.getChildren());

        for (ASTNode child : children) {
            transformChildren(child);
        }
    }

    private Expression transformExpression(Expression expression) {

        if (expression instanceof VariableReference) {
            VariableReference reference = (VariableReference) expression;

            Literal value =
                    variableValues.getFirst().get(reference.name);

            if (value != null) {
                return value;
            }

            return expression;
        }

        if (expression instanceof Operation) {
            Operation operation = (Operation) expression;

            Literal result = calculate(operation);

            if (result != null) {
                return result;
            }
        }

        return expression;
    }

    private Literal calculate(Operation operation) {

        Expression left = transformExpression(operation.lhs);
        Expression right = transformExpression(operation.rhs);

        operation.lhs = left;
        operation.rhs = right;

        if (!(left instanceof Literal) || !(right instanceof Literal)) {
            return null;
        }

        Literal leftValue = (Literal) left;
        Literal rightValue = (Literal) right;

        if (operation instanceof MultiplyOperation) {

            if (leftValue instanceof ScalarLiteral
                    && rightValue instanceof PixelLiteral) {
                return new PixelLiteral(
                        ((ScalarLiteral) leftValue).value
                                * ((PixelLiteral) rightValue).value
                );
            }

            if (leftValue instanceof PixelLiteral
                    && rightValue instanceof ScalarLiteral) {
                return new PixelLiteral(
                        ((PixelLiteral) leftValue).value
                                * ((ScalarLiteral) rightValue).value
                );
            }

            if (leftValue instanceof ScalarLiteral
                    && rightValue instanceof ScalarLiteral) {
                return new ScalarLiteral(
                        ((ScalarLiteral) leftValue).value
                                * ((ScalarLiteral) rightValue).value
                );
            }
        }

        if (operation instanceof AddOperation) {

            if (leftValue instanceof PixelLiteral
                    && rightValue instanceof PixelLiteral) {
                return new PixelLiteral(
                        ((PixelLiteral) leftValue).value
                                + ((PixelLiteral) rightValue).value
                );
            }

            if (leftValue instanceof ScalarLiteral
                    && rightValue instanceof ScalarLiteral) {
                return new ScalarLiteral(
                        ((ScalarLiteral) leftValue).value
                                + ((ScalarLiteral) rightValue).value
                );
            }

            if (leftValue instanceof PercentageLiteral
                    && rightValue instanceof PercentageLiteral) {
                return new PercentageLiteral(
                        ((PercentageLiteral) leftValue).value
                                + ((PercentageLiteral) rightValue).value
                );
            }
        }

        if (operation instanceof SubtractOperation) {

            if (leftValue instanceof PixelLiteral
                    && rightValue instanceof PixelLiteral) {
                return new PixelLiteral(
                        ((PixelLiteral) leftValue).value
                                - ((PixelLiteral) rightValue).value
                );
            }

            if (leftValue instanceof ScalarLiteral
                    && rightValue instanceof ScalarLiteral) {
                return new ScalarLiteral(
                        ((ScalarLiteral) leftValue).value
                                - ((ScalarLiteral) rightValue).value
                );
            }

            if (leftValue instanceof PercentageLiteral
                    && rightValue instanceof PercentageLiteral) {
                return new PercentageLiteral(
                        ((PercentageLiteral) leftValue).value
                                - ((PercentageLiteral) rightValue).value
                );
            }
        }

        return null;
    }
}
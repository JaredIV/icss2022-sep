package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.VariableAssignment;
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
                ExpressionType type = null;

                if (assignment.expression instanceof ColorLiteral) {
                    type = ExpressionType.COLOR;
                } else if (assignment.expression instanceof PixelLiteral) {
                    type = ExpressionType.PIXEL;
                } else if (assignment.expression instanceof BoolLiteral) {
                    type = ExpressionType.BOOL;
                } else if (assignment.expression instanceof PercentageLiteral) {
                    type = ExpressionType.PERCENTAGE;
                } else if (assignment.expression instanceof ScalarLiteral) {
                    type = ExpressionType.SCALAR;
                }

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

            }
        }
    }

}

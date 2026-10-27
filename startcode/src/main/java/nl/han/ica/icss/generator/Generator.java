package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.AST;
import nl.han.ica.icss.ast.ASTNode;
import nl.han.ica.icss.ast.Stylerule;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.selectors.*;

public class Generator {

	public String generate(AST ast) {
		StringBuilder cssCode = new StringBuilder();

		for (ASTNode node : ast.root.body) {
			if (node instanceof Stylerule) {
				cssCode.append(generateStyleruleToCSS((Stylerule) node));
				cssCode.append("\n");
			}
		}

		return cssCode.toString();
	}

	private String generateStyleruleToCSS(Stylerule rule) {
		StringBuilder css = new StringBuilder();


		css.append(getSelectorName(rule.selectors.get(0)));

		css.append(" {\n");

		for (ASTNode node : rule.body) {

			if (node instanceof Declaration) {
				Declaration declaration = (Declaration) node;

				css.append("    ");
				css.append(declaration.property.name);
				css.append(": ");
				css.append(generateExpression(declaration.expression));
				css.append(";\n");
			}
		}

		css.append("}\n");

		return css.toString();
	}

	private String getSelectorName(Selector selector) {
		if (selector instanceof ClassSelector
				|| selector instanceof IdSelector
				|| selector instanceof TagSelector) {
			return selector.toString();
		}
		return "";
	}

	private String generateExpression(Expression expression) {
		if (expression instanceof ColorLiteral) {
			return ((ColorLiteral) expression).value;
		}
		if (expression instanceof PixelLiteral) {
			return ((PixelLiteral) expression).value + "px";
		}
		if (expression instanceof PercentageLiteral) {
			return ((PercentageLiteral) expression).value + "%";
		}
		return "";
	}
}

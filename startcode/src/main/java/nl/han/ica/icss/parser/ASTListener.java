package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

	//Accumulator attributes:
	private AST ast;
	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}

	public AST getAST() {
		return ast;
	}

	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.push(new Stylesheet());
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		ast.setRoot((Stylesheet) currentContainer.pop());
	}

	@Override
	public void enterVariable(ICSSParser.VariableContext ctx) {
		String variableName = ctx.CAPITAL_IDENT().getText();

		ASTNode assignment = new VariableAssignment();
		currentContainer.push(assignment);

		assignment.addChild(new VariableReference(variableName));
	}

	@Override
	public void exitVariable(ICSSParser.VariableContext ctx) {
		ASTNode variable = currentContainer.peek();

		variable.addChild(buildExpression(ctx.expression()));

		currentContainer.pop();
		currentContainer.peek().addChild(variable);
	}

	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		currentContainer.push(new Stylerule());
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		ASTNode styleRule = currentContainer.pop();
		currentContainer.peek().addChild(styleRule);
	}

	@Override
	public void enterSelector(ICSSParser.SelectorContext ctx) {
		String text = ctx.getText();

		if (text.startsWith("#")) {
			currentContainer.push(new IdSelector(text));
		} else if (text.startsWith(".")) {
			currentContainer.push(new ClassSelector(text));
		} else {
			currentContainer.push(new TagSelector(text));
		}
	}

	@Override
	public void exitSelector(ICSSParser.SelectorContext ctx) {
		ASTNode selector = currentContainer.pop();
		currentContainer.peek().addChild(selector);
	}

	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		String propertyName = ctx.LOWER_IDENT().getText();
		currentContainer.push(new Declaration(propertyName));
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		ASTNode declaration = currentContainer.peek();

		declaration.addChild(buildExpression(ctx.expression()));

		currentContainer.pop();
		currentContainer.peek().addChild(declaration);
	}


	private ASTNode buildExpression(ICSSParser.ExpressionContext ctx) {
		return buildAdditiveExpression(ctx.additiveExpression());
	}

	private ASTNode buildAdditiveExpression(ICSSParser.AdditiveExpressionContext ctx) {

		ASTNode result = buildMultiplicativeExpression(ctx.multiplicativeExpression(0));

		for (int i = 1; i < ctx.multiplicativeExpression().size(); i++) {
			ASTNode right = buildMultiplicativeExpression(ctx.multiplicativeExpression(i));

			String operator = ctx.getChild(2 * i - 1).getText();

			ASTNode operation;

			if (operator.equals("+")) {
				operation = new AddOperation();
			} else {
				operation = new SubtractOperation();
			}

			operation.addChild(result);
			operation.addChild(right);

			result = operation;
		}

		return result;
	}

	private ASTNode buildMultiplicativeExpression(ICSSParser.MultiplicativeExpressionContext ctx) {

		ASTNode result = buildPrimaryExpression(ctx.primaryExpression(0));

		for (int i = 1; i < ctx.primaryExpression().size(); i++) {
			ASTNode right = buildPrimaryExpression(ctx.primaryExpression(i));

			ASTNode operation = new MultiplyOperation();
			operation.addChild(result);
			operation.addChild(right);

			result = operation;
		}

		return result;
	}

	private ASTNode buildPrimaryExpression(
			ICSSParser.PrimaryExpressionContext ctx) {

		if (ctx.literal() != null) {
			return buildLiteral(ctx.literal());
		}

		return buildExpression(ctx.expression());
	}

	private ASTNode buildLiteral(ICSSParser.LiteralContext ctx) {
		String value = ctx.getText();

		if (ctx.COLOR() != null) {
			return new ColorLiteral(value);
		} else if (ctx.PIXELSIZE() != null) {
			return new PixelLiteral(value);
		} else if (ctx.PERCENTAGE() != null) {
			return new PercentageLiteral(value);
		} else if (ctx.SCALAR() != null) {
			return new ScalarLiteral(value);
		} else if (ctx.TRUE() != null) {
			return new BoolLiteral(true);
		} else if (ctx.FALSE() != null) {
			return new BoolLiteral(false);
		} else if (ctx.CAPITAL_IDENT() != null) {
			return new VariableReference(value);
		}

		throw new IllegalArgumentException("Onbekende literal: " + value);
	}


	@Override
	public void enterExpression(ICSSParser.ExpressionContext ctx) {
	}

	@Override
	public void exitExpression(ICSSParser.ExpressionContext ctx) {
	}

	@Override
	public void enterAdditiveExpression(ICSSParser.AdditiveExpressionContext ctx) {
	}

	@Override
	public void exitAdditiveExpression(ICSSParser.AdditiveExpressionContext ctx) {
	}

	@Override
	public void enterMultiplicativeExpression(ICSSParser.MultiplicativeExpressionContext ctx) {
	}

	@Override
	public void exitMultiplicativeExpression(ICSSParser.MultiplicativeExpressionContext ctx) {
	}

	@Override
	public void enterPrimaryExpression(ICSSParser.PrimaryExpressionContext ctx) {
	}

	@Override
	public void exitPrimaryExpression(ICSSParser.PrimaryExpressionContext ctx) {
	}

	@Override
	public void enterLiteral(ICSSParser.LiteralContext ctx) {
	}

	@Override
	public void exitLiteral(ICSSParser.LiteralContext ctx) {
	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
	}

	@Override
	public void visitTerminal(TerminalNode node) {
	}

	@Override
	public void visitErrorNode(ErrorNode node) {
	}
}

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
 * Bouwt de AST (Abstract Syntax Tree) op uit de parse tree van ANTLR.
 *
 * De listener wordt automatisch aangeroepen wanneer ANTLR een regel
 * binnenkomt of verlaat.
 *
 * currentContainer houdt bij aan welk AST-element nieuwe onderdelen
 * moeten worden toegevoegd.
 */
public class ASTListener extends ICSSBaseListener {

	//Accumulator attributes:
	private AST ast;
	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	/**
	 * Maakt een lege AST en een lege stack aan.
	 *
	 * Invoer: geen.
	 * Werking: initialiseert de AST en de stack.
	 * Uitvoer: een ASTListener die klaar is om een AST op te bouwen.
	 */
	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}

	/**
	 * Geeft de opgebouwde AST terug.
	 *
	 * Invoer: geen.
	 * Werking: haalt de AST op die door de listener is opgebouwd.
	 * Uitvoer: de AST.
	 */
	public AST getAST() {
		return ast;
	}

	/**
	 * Wordt aangeroepen wanneer ANTLR de stylesheet-regel binnenkomt.
	 *
	 * Invoer: de parse-tree-context van de stylesheet.
	 * Werking: maakt een nieuwe Stylesheet en zet deze op de stack.
	 * Uitvoer: geen; de Stylesheet staat nu boven op de stack.
	 */
	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.push(new Stylesheet());
	}

	/**
	 * Wordt aangeroepen wanneer ANTLR klaar is met de stylesheet.
	 *
	 * Invoer: de parse-tree-context van de stylesheet.
	 * Werking: haalt de Stylesheet van de stack en stelt deze in als root.
	 * Uitvoer: geen; de AST heeft nu zijn hoofdelement.
	 */
	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		ast.setRoot((Stylesheet) currentContainer.pop());
	}

    /**
     * Wordt aangeroepen wanneer een variabeletoekenning begint.
     *
     * Voorbeeld: ParWidth := 500px;
     *
     * Invoer: de parse-tree-context van de variabele.
     * Werking: maakt een VariableAssignment en voegt de naam als
     * eerste kind toe.
     * Uitvoer: geen; de assignment staat boven op de stack.
     */
	@Override
	public void enterVariable(ICSSParser.VariableContext ctx) {
		String variableName = ctx.CAPITAL_IDENT().getText();

		ASTNode assignment = new VariableAssignment();
		currentContainer.push(assignment);

		assignment.addChild(new VariableReference(variableName));
	}

	/**
	 * Wordt aangeroepen wanneer een variabeletoekenning klaar is.
	 *
	 * Invoer: de parse-tree-context met de expressie van de variabele.
	 * Werking: bouwt de expressie, voegt deze als tweede kind toe,
	 * haalt de assignment van de stack en voegt hem toe aan de ouder.
	 * Uitvoer: geen; de volledige VariableAssignment zit in de AST.
	 */
	@Override
	public void exitVariable(ICSSParser.VariableContext ctx) {
		ASTNode variable = currentContainer.peek();

		variable.addChild(buildExpression(ctx.expression()));

		currentContainer.pop();
		currentContainer.peek().addChild(variable);
	}

	/**
	 * Wordt aangeroepen wanneer een stylerule begint.
	 *
	 * Voorbeeld: p { ... }
	 *
	 * Invoer: de parse-tree-context van de stylerule.
	 * Werking: maakt een nieuwe Stylerule en zet deze op de stack.
	 * Uitvoer: geen; de Stylerule is klaar om kinderen te ontvangen.
	 */
	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		currentContainer.push(new Stylerule());
	}

	/**
	 * Wordt aangeroepen wanneer een stylerule klaar is.
	 *
	 * Invoer: de parse-tree-context van de stylerule.
	 * Werking: haalt de Stylerule van de stack en voegt deze toe
	 * aan de ouder, meestal de Stylesheet.
	 * Uitvoer: geen; de Stylerule is toegevoegd aan de AST.
	 */
	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		ASTNode styleRule = currentContainer.pop();
		currentContainer.peek().addChild(styleRule);
	}

	/**
	 * Wordt aangeroepen wanneer een selector wordt gelezen.
	 *
	 * Voorbeelden: p, #menu of .menu.
	 *
	 * Invoer: de parse-tree-context van de selector.
	 * Werking: kijkt naar het eerste teken en maakt het juiste type:
	 * # betekent IdSelector, . betekent ClassSelector en anders TagSelector.
	 * Uitvoer: geen; de selector staat boven op de stack.
	 */
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

	/**
	 * Wordt aangeroepen wanneer de selector klaar is.
	 *
	 * Invoer: de parse-tree-context van de selector.
	 * Werking: haalt de selector van de stack en voegt deze toe
	 * aan de huidige Stylerule.
	 * Uitvoer: geen; de Stylerule heeft nu een selector.
	 */
	@Override
	public void exitSelector(ICSSParser.SelectorContext ctx) {
		ASTNode selector = currentContainer.pop();
		currentContainer.peek().addChild(selector);
	}

	/**
	 * Wordt aangeroepen wanneer een declaratie begint.
	 *
	 * Voorbeeld: color: #ff0000;
	 *
	 * Invoer: de parse-tree-context van de declaratie.
	 * Werking: leest de propertynaam en maakt een Declaration.
	 * Uitvoer: geen; de Declaration staat boven op de stack.
	 */
	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		String propertyName = ctx.LOWER_IDENT().getText();
		currentContainer.push(new Declaration(propertyName));
	}

	/**
	 * Wordt aangeroepen wanneer een declaratie klaar is.
	 *
	 * Invoer: de parse-tree-context met de waarde van de declaratie.
	 * Werking: bouwt de expressie van de waarde, voegt deze toe aan
	 * de Declaration en voegt de Declaration toe aan de ouder.
	 * Uitvoer: geen; de declaratie zit in de AST.
	 */
	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		ASTNode declaration = currentContainer.peek();

		declaration.addChild(buildExpression(ctx.expression()));

		currentContainer.pop();
		currentContainer.peek().addChild(declaration);
	}

	/**
	 * Wordt aangeroepen wanneer een if-statement begint.
	 *
	 * Voorbeeld: if[AdjustColor] { ... }
	 *
	 * Invoer: de parse-tree-context met de voorwaarde.
	 * Werking: maakt een IfClause, bouwt de voorwaarde en voegt
	 * deze als eerste kind toe. Daarna komt de IfClause op de stack.
	 * Uitvoer: geen; de body kan nu kinderen aan de IfClause toevoegen.
	 */
	@Override
	public void enterConditional(ICSSParser.ConditionalContext ctx) {
		IfClause ifClause = new IfClause();

		ifClause.addChild(buildExpression(ctx.expression()));

		currentContainer.push(ifClause);
	}

	/**
	 * Wordt aangeroepen wanneer een if-statement helemaal klaar is,
	 * inclusief een eventuele else.
	 *
	 * Invoer: de parse-tree-context van de if.
	 * Werking: haalt de IfClause van de stack en voegt hem toe aan
	 * de ouder, bijvoorbeeld een Stylerule of een andere IfClause.
	 * Uitvoer: geen; de IfClause zit in de AST.
	 */
	@Override
	public void exitConditional(ICSSParser.ConditionalContext ctx) {
		ASTNode ifClause = currentContainer.pop();

		currentContainer.peek().addChild(ifClause);
	}

	/**
	 * Wordt aangeroepen wanneer de else-body begint.
	 *
	 * Invoer: de parse-tree-context van de else.
	 * Werking: maakt een ElseClause en zet deze op de stack.
	 * Uitvoer: geen; declaraties in de else-body worden hieraan toegevoegd.
	 */
	@Override
	public void enterElseClause(ICSSParser.ElseClauseContext ctx) {
		currentContainer.push(new ElseClause());
	}

	/**
	 * Wordt aangeroepen wanneer de else-body klaar is.
	 *
	 * Invoer: de parse-tree-context van de else.
	 * Werking: haalt de ElseClause van de stack en voegt hem toe
	 * aan de bijbehorende IfClause.
	 * Uitvoer: geen; de ElseClause is onderdeel van de IfClause.
	 */
	@Override
	public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
		ASTNode elseClause = currentContainer.pop();

		currentContainer.peek().addChild(elseClause);
	}

	/**
	 * Bouwt een volledige expressie op.
	 *
	 * Invoer: de parse-tree-context van een expression.
	 * Werking: geeft het werk door aan buildAdditiveExpression,
	 * zodat + en - en de onderliggende berekeningen worden verwerkt.
	 * Uitvoer: de ASTNode die de expressie voorstelt.
	 */
	private ASTNode buildExpression(ICSSParser.ExpressionContext ctx) {
		return buildAdditiveExpression(ctx.additiveExpression());
	}

	/**
	 * Bouwt een expressie met + en - op.
	 *
	 * Voorbeeld: A + 2 - 3.
	 *
	 * Invoer: de parse-tree-context van een additiveExpression.
	 * Werking: bouwt eerst het linkerdeel. Daarna verwerkt de methode
	 * iedere volgende operand en maakt een AddOperation of SubtractOperation.
	 * Bij meerdere operatoren wordt het resultaat telkens verder opgebouwd.
	 * Uitvoer: de ASTNode van de volledige optel-/aftreksom.
	 */
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

	/**
	 * Bouwt een expressie met vermenigvuldigingen op.
	 *
	 * Voorbeeld: 2 * 10px.
	 *
	 * Invoer: de parse-tree-context van een multiplicativeExpression.
	 * Werking: bouwt de eerste operand en maakt voor iedere volgende
	 * operand een MultiplyOperation met links en rechts een kind.
	 * Uitvoer: de ASTNode van de volledige vermenigvuldiging.
	 */
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

	/**
	 * Bouwt één primair onderdeel van een expressie.
	 *
	 * Voorbeelden: 20px, ParWidth of [ParWidth + 20px].
	 *
	 * Invoer: de parse-tree-context van een primaryExpression.
	 * Werking: als er een literal is, bouwt de methode die literal.
	 * Anders bouwt hij de expressie binnen de vierkante haakjes.
	 * Uitvoer: de ASTNode van dit expressieonderdeel.
	 */
	private ASTNode buildPrimaryExpression(
			ICSSParser.PrimaryExpressionContext ctx) {

		if (ctx.literal() != null) {
			return buildLiteral(ctx.literal());
		}

		return buildExpression(ctx.expression());
	}

	/**
	 * Zet een literal of variabelenaam om naar het juiste AST-type.
	 *
	 * Voorbeelden: #ff0000, 20px, 50%, 10, TRUE of ParWidth.
	 *
	 * Invoer: de parse-tree-context van een literal.
	 * Werking: bepaalt het type token en maakt het bijbehorende object.
	 * Uitvoer: een ColorLiteral, PixelLiteral, PercentageLiteral,
	 * ScalarLiteral, BoolLiteral of VariableReference.
	 */
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

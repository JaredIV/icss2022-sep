package nl.han.ica.icss.parser;

import java.util.Stack;


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

	/**
	 Creates the root {@link Stylesheet} node when entering the stylesheet.
	 that all child nodes can be added to it while parsing.</p>
	 */
	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		ASTNode stylesheet = new Stylesheet();
		currentContainer.push(stylesheet);
	}
	/**

	 Sets the completed {@link Stylesheet} as the root of the AST.
	 as the root node of the AST.</p>
	 */

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		ast.setRoot((Stylesheet) currentContainer.pop());
	}
	/**

	 Creates a new {@link Stylerule} when entering a style rule.
	 selectors and declarations can be added as child nodes.</p>
	 */
	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		ASTNode styleRule = new Stylerule();
		currentContainer.push(styleRule);
	}
	/**

	 Adds the completed {@link Stylerule} to its parent node.
	 as a child of the current parent node.</p>
	 */
	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		ASTNode styleRule = currentContainer.pop();
		currentContainer.peek().addChild(styleRule);
	}
	/**
	  Creates the appropriate selector node based on the parsed selector text.
	  <p>If the selector starts with {@code #}, an {@link IdSelector} is created.
	  If the selector starts with {@code .}, a {@link ClassSelector} is created.
	  Otherwise, a {@link TagSelector} is created.</p>

	  <p>The created selector is pushed onto the container stack so that it can
	  be added to the current {@link Stylerule} when parsing is complete.</p>
	 */
	@Override
	public void enterSelector(ICSSParser.SelectorContext ctx) {
		String text = ctx.getText();
		if (text.contains("#")){
			ASTNode idSelector = new IdSelector(ctx.getText());
			currentContainer.push(idSelector);
		}
		else if(text.contains(".")){
			ASTNode idSelector = new ClassSelector(ctx.getText());
			currentContainer.push(idSelector);
		}
		else{
			ASTNode idSelector = new TagSelector(ctx.getText());
			currentContainer.push(idSelector);
		}
	 }
	/**

	 Adds the completed selector to its parent style rule.
	 as a child of the current parent node.</p>
	 */
	@Override
	public void exitSelector(ICSSParser.SelectorContext ctx) {
		ASTNode selector = currentContainer.pop();
		currentContainer.peek().addChild(selector);
	}
	/**

	 Creates a {@link Declaration} containing the property name.
	 property name before the colon is stored in the AST.</p>
	 */
	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		String text = ctx.getText();

		int colonIndex = text.indexOf(':');
		if (colonIndex != -1) {
			text = text.substring(0, colonIndex);
		}

		ASTNode declaration = new Declaration(text);
		currentContainer.push(declaration);
	}
	/**

	 Adds the completed {@link Declaration} to its parent node.
	 as a child of the current parent node.</p>
	 */
	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		ASTNode declaration = currentContainer.pop();
		currentContainer.peek().addChild(declaration);
	}
	/**

	 Creates an AST literal node based on the type of literal parsed.
	 by {@link PixelLiteral}, and percentages by {@link PercentageLiteral}.</p>
	 */
	@Override
	public void enterLiteral(ICSSParser.LiteralContext ctx) {
		String declartionValeu = ctx.getText();

		if(ctx.COLOR() != null){
			ASTNode coller = new ColorLiteral(declartionValeu);
			currentContainer.push(coller);
		}
		if(ctx.PIXELSIZE() != null){
			ASTNode pixel = new PixelLiteral(declartionValeu);
			currentContainer.push(pixel);
		}
		if(ctx.PERCENTAGE() != null){
			ASTNode percentage = new PercentageLiteral(declartionValeu);
			currentContainer.push(percentage);
		}

	 }
	/**
	 Adds the completed literal to its parent node.
	 as a child of the current parent node.</p>
	 */
	@Override
	public void exitLiteral(ICSSParser.LiteralContext ctx) {
		ASTNode literal = currentContainer.pop();
		currentContainer.peek().addChild(literal);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override
	public void enterEveryRule(ParserRuleContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override
	public void exitEveryRule(ParserRuleContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override
	public void visitTerminal(TerminalNode node) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override
	public void visitErrorNode(ErrorNode node) { }
    
}
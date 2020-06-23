package at.lagerfeuer.lox;

import at.lagerfeuer.lox.ast.Expr;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    private Expr parse(String input) {
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.scanTokens());
        return parser.parse();
    }

    @Test
    void addition() {
        String input = "1 + 2 + 3";
        Expr expr = parse(input);

        Assert.assertTrue(expr instanceof Expr.Binary);
        Expr.Binary bexpr = (Expr.Binary) expr;

        System.out.println(bexpr.left.getClass().toString());
        Assert.assertTrue(bexpr.left instanceof Expr.Binary);
        Assert.assertSame(bexpr.operator.type, TokenType.PLUS);
        Assert.assertTrue(bexpr.right instanceof Expr.Literal);

        Expr.Binary left = (Expr.Binary) bexpr.left;
        Assert.assertTrue(left.left instanceof Expr.Literal);
        Assert.assertSame(left.operator.type, TokenType.PLUS);
        Assert.assertTrue(left.right instanceof Expr.Literal);
    }
}
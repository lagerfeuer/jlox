package at.lagerfeuer.lox;

import at.lagerfeuer.lox.ast.Expr;
import at.lagerfeuer.lox.ast.Stmt;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    private List<Stmt> parse(String input) {
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.scanTokens());
        return parser.parse();
    }

    private Expr parseExpr(String input) {
        List<Stmt> stmts = parse(input);
        return (stmts == null || stmts.isEmpty())
                ? null
                : ((Stmt.Expression) stmts.get(0)).expr;
    }

    private void assertLiteral(Expr literal, int ref) {
        double litval = (double) ((Expr.Literal) literal).value;
        assertEquals((double) ref, litval, 0.001);
    }

    private void assertLiteral(Expr literal, Object ref) {
        Object litval = ((Expr.Literal) literal).value;
        if (litval instanceof Double)
            assertEquals((double) ref, (double) litval, 0.001);
        else
            assertEquals(ref, litval);
    }

    @Test
    void equality() {
        String input = "1 != 2;";
        Expr expr = parseExpr(input);

        assertTrue(expr instanceof Expr.Binary);
        Expr.Binary bexpr = (Expr.Binary) expr;

        assertLiteral(bexpr.left, 1);
        assertSame(bexpr.operator.type, TokenType.BANG_EQUAL);
        assertLiteral(bexpr.right, 2);
    }

    @Test
    void comparison() {
        String input = "1 < 2;";
        Expr expr = parseExpr(input);

        assertTrue(expr instanceof Expr.Binary);
        Expr.Binary bexpr = (Expr.Binary) expr;

        assertLiteral(bexpr.left, 1);
        assertSame(bexpr.operator.type, TokenType.LESS);
        assertLiteral(bexpr.right, 2);
    }

    @Test
    void addition() {
        String input = "1 + 2 + 3;";
        Expr expr = parseExpr(input);

        assertTrue(expr instanceof Expr.Binary);
        Expr.Binary bexpr = (Expr.Binary) expr;

        assertTrue(bexpr.left instanceof Expr.Binary);
        assertSame(bexpr.operator.type, TokenType.PLUS);
        assertTrue(bexpr.right instanceof Expr.Literal);

        Expr.Binary left = (Expr.Binary) bexpr.left;
        assertTrue(left.left instanceof Expr.Literal);
        assertSame(left.operator.type, TokenType.PLUS);
        assertTrue(left.right instanceof Expr.Literal);
    }

    @Test
    void multiplication() {
        String input = "1 * 2 * 3;";
        Expr expr = parseExpr(input);

        assertTrue(expr instanceof Expr.Binary);
        Expr.Binary bexpr = (Expr.Binary) expr;

        assertTrue(bexpr.left instanceof Expr.Binary);
        assertSame(bexpr.operator.type, TokenType.STAR);
        assertTrue(bexpr.right instanceof Expr.Literal);

        Expr.Binary left = (Expr.Binary) bexpr.left;
        assertTrue(left.left instanceof Expr.Literal);
        assertSame(left.operator.type, TokenType.STAR);
        assertTrue(left.right instanceof Expr.Literal);
    }

    @Test
    void additionAndMultiplication() {
        String input = "1 + 2 * 3;";
        Expr expr = parseExpr(input);

        assertTrue(expr instanceof Expr.Binary);
        Expr.Binary bexpr = (Expr.Binary) expr;

        assertTrue(bexpr.left instanceof Expr.Literal);
        assertSame(bexpr.operator.type, TokenType.PLUS);
        assertTrue(bexpr.right instanceof Expr.Binary);

        Expr.Binary right = (Expr.Binary) bexpr.right;
        assertTrue(right.left instanceof Expr.Literal);
        assertSame(right.operator.type, TokenType.STAR);
        assertTrue(right.right instanceof Expr.Literal);
    }

    @Test
    void unary() {
        String input = "!!true;";
        Expr expr = parseExpr(input);

        assertTrue(expr instanceof Expr.Unary);
        Expr.Unary unary = (Expr.Unary) expr;
        assertSame(unary.operator.type, TokenType.BANG);
        assertTrue(unary.right instanceof Expr.Unary);

        unary = (Expr.Unary) unary.right;
        assertSame(unary.operator.type, TokenType.BANG);
        assertLiteral(unary.right, true);
    }

    @Test
    void parenthesis() {
        String input = "(nil);";
        Expr expr = parseExpr(input);

        assertTrue(expr instanceof Expr.Grouping);
        Expr.Grouping gexpr = (Expr.Grouping) expr;
        assertLiteral(gexpr.expr, null);
    }

    @Test
    void ternary() {
        String input = "true ? 1 : 2;";
        Expr expr = parseExpr(input);

        assertTrue(expr instanceof Expr.Ternary);
        Expr.Ternary texpr = (Expr.Ternary) expr;

        assertLiteral(texpr.condition, true);
        assertLiteral(texpr.trueBranch, 1.0);
        assertLiteral(texpr.falseBranch, 2.0);
    }

    @Test
    void nestedTernary() {
        String input = "false ? true ? 1 : 2 : 3;";
        Expr expr = parseExpr(input);

        assertTrue(expr instanceof Expr.Ternary);
        Expr.Ternary texpr = (Expr.Ternary) expr;

        assertLiteral(texpr.condition, false);
        assertTrue(texpr.trueBranch instanceof Expr.Ternary);
        assertLiteral(texpr.falseBranch, 3.0);

        Expr.Ternary trueexpr = (Expr.Ternary) texpr.trueBranch;

        assertLiteral(trueexpr.condition, true);
        assertLiteral(trueexpr.trueBranch, 1.0);
        assertLiteral(trueexpr.falseBranch, 2.0);
    }

    @Test
    void comma() {
        String input = "1, 2, true ? 3 : -1, false;";
        Expr expr = parseExpr(input);

        assertTrue(expr instanceof Expr.Comma);
        Expr.Comma comma = (Expr.Comma) expr;
        assertEquals(4, comma.exprs.size());
        assertLiteral(comma.exprs.get(0), 1);
        assertLiteral(comma.exprs.get(1), 2);
        assertTrue(comma.exprs.get(2) instanceof Expr.Ternary);
        assertLiteral(comma.exprs.get(3), false);
    }

    @Test
    void invalidInput() {
        String input = "( 1 + 2; var a = 1;";
        List<Stmt> stmts = parse(input);
        assertEquals(0, stmts.size());

        input = "+ 2 var a = 1;";
        stmts = parse(input);
        assertEquals(0, stmts.size());

        input = "+ 2;";
        Expr expr = parseExpr(input);
        assertNull(expr);
    }
}
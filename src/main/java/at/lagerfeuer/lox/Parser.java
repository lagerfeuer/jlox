package at.lagerfeuer.lox;

import at.lagerfeuer.lox.ast.Expr;
import at.lagerfeuer.lox.ast.Stmt;

import java.util.ArrayList;
import java.util.List;

import static at.lagerfeuer.lox.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> TOKENS;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.TOKENS = tokens;
    }

    private Token peek() {
        return TOKENS.get(current);
    }

    private Token previous() {
        return TOKENS.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    /**
     * After a ParseError has been thrown, synchronize the parser, i.e. get "back on track" and try parsing the rest.
     */
    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON)
                return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        synchronize();
        return new ParseError();
    }

    public List<Stmt> parse() {
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()) {
            Stmt stmt = declaration();
            if (stmt != null)
                stmts.add(stmt);
        }
        return stmts;
    }

    private Stmt declaration() {
        try {
            if (match(VAR))
                return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (match(EQUAL))
            initializer = expression();
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(PRINT))
            return printStatement();
        if (match(LBRACE))
            return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt.Print printStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Print(expr);
    }

    private Stmt.Expression expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> stmts = new ArrayList<>();
        while (!check(RBRACE) && !isAtEnd())
            stmts.add(declaration());
        consume(RBRACE, "Expect '}' after block.");
        return stmts;
    }

    private Expr expression() {
        return comma();
    }

    private Expr comma() {
        List<Expr> list = new ArrayList<>();
        do {
            list.add(assignment());
        } while (match(COMMA));
        return (list.size() == 1) ? list.get(0) : new Expr.Comma(list);
    }

    private Expr assignment() {
        Expr expr = ternary();
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }
            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr ternary() {
        Expr expr = equality();
        if (match(QUESTION)) {
            Expr trueBranch = ternary();
            consume(COLON, "Ternary operator expects ':'");
            Expr falseBranch = equality();
            return new Expr.Ternary(expr, trueBranch, falseBranch);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = addition();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr addition() {
        Expr expr = multiplication();
        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr multiplication() {
        Expr expr = unary();
        while (match(STAR, SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        // TODO only use one instance for true, false and nil
        if (match(FALSE))
            return new Expr.Literal(false);
        if (match(TRUE))
            return new Expr.Literal(true);
        if (match(NIL))
            return new Expr.Literal(null);

        if (match(IDENTIFIER))
            return new Expr.Variable(previous());
        if (match(NUMBER, STRING))
            return new Expr.Literal(previous().literal);

        if (match(LPAREN)) {
            Expr expr = expression();
            consume(RPAREN, "Expect ')' after Expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect Expression.");
    }
}

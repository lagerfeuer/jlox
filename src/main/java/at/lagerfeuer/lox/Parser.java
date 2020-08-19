package at.lagerfeuer.lox;

import at.lagerfeuer.lox.ast.Expr;
import at.lagerfeuer.lox.ast.Stmt;

import java.util.ArrayList;
import java.util.Arrays;
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

    private Token reverse() {
        current--;
        return (current > 0) ? previous() : null;
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
            if (match(CLASS))
                return classDeclaration();
            if (match(VAR))
                return varDeclaration();
            if (match(FUN))
                if (check(IDENTIFIER))
                    return function("function");
                else
                    reverse(); // un-advance token to be parsed as lambda expression statement
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");
        consume(LBRACE, "Expect '{' before class body.");

        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(RBRACE) && !isAtEnd())
            methods.add(function("method"));

        consume(RBRACE, "Expect '}' after class body.");
        return new Stmt.Class(name, methods);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (match(EQUAL))
            initializer = expression();
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect " + kind + " identifier.");

        consume(LPAREN, "Expect '(' after '" + kind + "' identifier.");
        List<Token> parameters = parameterList(kind);
        consume(RPAREN, "Expect ')' after '" + kind + "' parameter list.");

        consume(LBRACE, "Expect '{' before '" + kind + "' body.");
        List<Stmt> body = block();

        return new Stmt.Function(name, parameters, body);
    }

    private List<Token> parameterList(String kind) {
        List<Token> parameters = new ArrayList<>();
        if (!check(RPAREN)) {
            do {
                if (parameters.size() > Constants.MAX_CALL_ARGUMENTS)
                    throw error(peek(), String.format("%s cannot have more than %d parameters",
                            kind, Constants.MAX_CALL_ARGUMENTS));
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }
        return parameters;
    }

    private Stmt statement() {
        if (match(RETURN))
            return returnStatement();
        if (match(WHILE))
            return whileStatement();
        if (match(FOR))
            return forStatement();
        if (match(LBRACE))
            return new Stmt.Block(block());
        if (match(IF))
            return ifStatement();
        if (match(BREAK))
            return breakStatement();
        return expressionStatement();
    }

    private Stmt.Return returnStatement() {
        Token token = previous();
        Expr expr = null;
        if (!check(SEMICOLON))
            expr = expression();
        consume(SEMICOLON, "Expect ';' after 'return'.");

        return new Stmt.Return(token, expr);
    }

    private Stmt.While whileStatement() {
        consume(LPAREN, "Expect '(' after 'while'");
        Expr condition = comma();
        consume(RPAREN, "Expect ')' after 'while' condition");

        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt forStatement() {
        consume(LPAREN, "Expect '(' after 'for'");

        Stmt initializer;
        if (match(SEMICOLON))
            initializer = null;
        else if (match(VAR))
            initializer = varDeclaration();
        else
            initializer = expressionStatement();

        Expr condition = null;
        if (!check(SEMICOLON))
            condition = comma();
        consume(SEMICOLON, "Expect ';' after 'for' loop condition");

        Expr increment = null;
        if (!check(SEMICOLON))
            increment = comma();
        consume(RPAREN, "Expect ')' after 'for' loop head");

        Stmt body = statement();

        // desugaring of for loop (convert to while loop)
        if (increment != null)
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));

        body = new Stmt.While(
                (condition == null) ? new Expr.Literal(true) : condition,
                body);

        if (initializer != null)
            body = new Stmt.Block(Arrays.asList(initializer, body));

        return body;
    }

    private Stmt.Expression expressionStatement() {
        Expr expr = comma();
        if (expr instanceof Expr.Lambda && match(LPAREN)) {
            expr = finishCall(expr);
        }
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

    private Stmt.If ifStatement() {
        consume(LPAREN, "Expect '(' after 'if'.");
        Expr condition = comma();
        consume(RPAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE))
            elseBranch = statement();

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt.Break breakStatement() {
        Stmt.Break breakStmt = new Stmt.Break(previous());
        consume(SEMICOLON, "Expect ';' after 'break'");
        return breakStmt;
    }

    private Expr comma() {
        List<Expr> list = new ArrayList<>();
        do {
            list.add(expression());
        } while (match(COMMA));
        return (list.size() == 1) ? list.get(0) : new Expr.Comma(list);
    }

    private Expr expression() {
        if (match(FUN))
            return lambda();
        return assignment();
    }

    private Expr.Lambda lambda() {
        String kind = "lambda";
        Token token = previous();
        consume(LPAREN, "Expect '(' after 'fun' in " + kind + ".");
        List<Token> parameters = parameterList(kind);
        consume(RPAREN, "Expect ')' after parameter list.");

        consume(LBRACE, "Expect '{' before '" + kind + "' body.");
        List<Stmt> body = block();

        return new Expr.Lambda(token, parameters, body);
    }

    private Expr assignment() {
        Expr expr = or();
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            }
            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = ternary();
        while (match(AND)) {
            Token operator = previous();
            Expr right = ternary();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr ternary() {
        Expr expr = equality();
        if (match(QUESTION)) {
            Expr thenBranch = ternary();
            consume(COLON, "Ternary operator expects ':'");
            Expr elseBranch = equality();
            return new Expr.Ternary(expr, thenBranch, elseBranch);
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
        return call();
    }

    private Expr call() {
        Expr expr = primary();
        while (true) {
            if (match(LPAREN))
                expr = finishCall(expr);
            else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else
                break;

        }
        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RPAREN)) {
            do {
                if (arguments.size() > Constants.MAX_CALL_ARGUMENTS)
                    throw error(peek(), String.format("Cannot have more than %d call arguments.",
                            Constants.MAX_CALL_ARGUMENTS));
                arguments.add(expression());
            } while (match(COMMA));
        }

        Token paren = consume(RPAREN, "Expect ')' after arguments in 'call'");
        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        // TODO only use one instance for true, false and nil
        if (match(FALSE))
            return new Expr.Literal(false);
        if (match(TRUE))
            return new Expr.Literal(true);
        if (match(NIL))
            return new Expr.Literal(null);

        if (match(THIS))
            return new Expr.This(previous());
        if (match(IDENTIFIER))
            return new Expr.Variable(previous());
        if (match(NUMBER, STRING))
            return new Expr.Literal(previous().literal);

        if (match(LPAREN)) {
            Expr expr = expression();
            consume(RPAREN, "Expect ')' after Expression.");
            return new Expr.Grouping(expr);
        }

        if (match(BREAK))
            throw error(previous(), "Unexpected 'break'.");

        throw error(peek(), "Expect Expression.");
    }
}

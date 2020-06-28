package at.lagerfeuer.lox;

import at.lagerfeuer.lox.ast.Expr;
import at.lagerfeuer.lox.ast.Stmt;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    public void interpret(List<Stmt> stmts) {
        try {
            for (Stmt stmt : stmts)
                execute(stmt);
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    /**
     * For testing purposes.
     *
     * @param expr expression to interpret
     * @return result of the evaluation as Object.
     */
    public Object interpret(Expr expr) {
        try {
            return expr.accept(this);
        } catch (RuntimeError error) {
            return null;
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        if (expr == null)
            return null;
        return expr.accept(this);
    }

    public static String stringify(Object object) {
        if (object == null)
            return "nil";

        // HACK Work around Java adding ".0" to integer-valued doubles.
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0"))
                text = text.substring(0, text.length() - 2);
            return text;
        }
        return object.toString();
    }

    private String str(Object object) {
        return stringify(object);
    }

    private boolean isTruthy(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof Boolean)
            return (boolean) obj;
        // NOTE empty containers or 0 are falsey too
        if (obj instanceof String)
            return !((String) obj).isEmpty();
        if (obj instanceof Double)
            return ((int) (double) obj) != 0;

        return true;
    }

    private boolean typeCheck(TokenType type, Object... objects) {
        Class<?> cls = null;
        switch (type) {
            case NUMBER:
                cls = Double.class;
                break;
            case STRING:
                cls = String.class;
                break;
            default:
                // unreachable
                return false;
        }
        return Arrays.stream(objects).allMatch(cls::isInstance);
    }

    private void checkNumberOperand(Token operator, Object operand) throws RuntimeError {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right)
            throws RuntimeError {
        if (left instanceof Double && right instanceof Double)
            return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private TokenType types(Object left, Object right)
            throws RuntimeError {
        if (left instanceof Double && right instanceof Double)
            return TokenType.NUMBER;
        if (left instanceof String && right instanceof String)
            return TokenType.STRING;
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case PLUS:
                if (typeCheck(TokenType.NUMBER, left, right))
                    return (double) left + (double) right;
                if (typeCheck(TokenType.STRING, left, right))
                    return (double) left + (double) right;
                if (left instanceof String || right instanceof String)
                    return str(left) + str(right);
                throw new RuntimeError(expr.operator,
                        "Expecting operands to be numbers or strings.");
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if ((double) right == 0)
                    throw new RuntimeError(expr.operator, "Division by 0");
                return (double) left / (double) right;
            case GREATER:
                switch (types(left, right)) {
                    case NUMBER:
                        return (double) left > (double) right;
                    case STRING:
                        return ((String) left).compareTo((String) right) > 0;
                    default:
                        throw new RuntimeError(expr.operator,
                                "Operands must be numbers or strings.");
                }
            case GREATER_EQUAL:
                switch (types(left, right)) {
                    case NUMBER:
                        return (double) left >= (double) right;
                    case STRING:
                        return ((String) left).compareTo((String) right) >= 0;
                    default:
                        throw new RuntimeError(expr.operator,
                                "Operands must be numbers or strings.");
                }
            case LESS:
                switch (types(left, right)) {
                    case NUMBER:
                        return (double) left < (double) right;
                    case STRING:
                        return ((String) left).compareTo((String) right) < 0;
                    default:
                        throw new RuntimeError(expr.operator,
                                "Operands must be numbers or strings.");
                }
            case LESS_EQUAL:
                switch (types(left, right)) {
                    case NUMBER:
                        return (double) left <= (double) right;
                    case STRING:
                        return ((String) left).compareTo((String) right) <= 0;
                    default:
                        throw new RuntimeError(expr.operator,
                                "Operands must be numbers or strings.");
                }
            case EQUAL_EQUAL:
                return Objects.equals(left, right);
            case BANG_EQUAL:
                return !Objects.equals(left, right);
        }

        // NOTE unreachable
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expr);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -((double) right);
            case BANG:
                return !isTruthy(right);
        }

        // NOTE unreachable
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return null;
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        if (isTruthy(evaluate(expr.condition)))
            return evaluate(expr.trueBranch);
        else
            return evaluate(expr.falseBranch);
    }

    @Override
    public Object visitCommaExpr(Expr.Comma expr) {
        Object last = null;
        for (Expr e : expr.exprs)
            last = evaluate(e);
        return last;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expr);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expr);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        return null;
    }
}

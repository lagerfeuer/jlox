package at.lagerfeuer.lox;

import at.lagerfeuer.lox.ast.Expr;

import java.util.Arrays;
import java.util.Objects;

public class Interpreter implements Expr.Visitor<Object> {
    public Object interpret(Expr expression) {
        try {
            Object result = evaluate(expression);
            // System.out.println(stringify(result));
            return result;
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
        return null;
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
                return (double) left / (double) right;
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
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
}
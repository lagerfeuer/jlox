package at.lagerfeuer.lox;

import at.lagerfeuer.lox.ast.Expr;
import at.lagerfeuer.lox.ast.Stmt;

import java.util.*;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    public final Environment globals = new Environment();
    private Environment env = globals;
    private Map<Expr, Integer> locals = new HashMap<>();

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native function>";
            }
        });

        globals.define("print", new LoxCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                System.out.println(stringify(arguments.get(0)));
                return null;
            }

            @Override
            public String toString() {
                return "<native function>";
            }
        });
    }

    /**
     * Interpret a program.
     *
     * @param stmts List of statements, parsed from the source code.
     */
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
            return evaluate(expr);
        } catch (RuntimeError error) {
            return null;
        }
    }

    public void executeBlock(List<Stmt> stmts, Environment env) {
        Environment previous = this.env;
        try {
            this.env = env;
            for (Stmt stmt : stmts)
                execute(stmt);
        } finally {
            this.env = previous;
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

    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null)
            return env.getAt(distance, name.lexeme);
        else
            return globals.get(name);
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
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if (distance != null)
            env.assignAt(distance, expr.name, value);
        else
            globals.assign(expr.name, value);

        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case PLUS:
                if (typeCheck(TokenType.NUMBER, left, right))
                    return (double) left + (double) right;
//                if (typeCheck(TokenType.STRING, left, right))
//                    return str(left) + str(right);
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
        return lookUpVariable(expr.name, expr);
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        if (isTruthy(evaluate(expr.condition)))
            return evaluate(expr.thenBranch);
        else
            return evaluate(expr.elseBranch);
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitCommaExpr(Expr.Comma expr) {
        Object last = null;
        for (Expr e : expr.exprs)
            last = evaluate(e);
        return last;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        switch (expr.operator.type) {
            case OR:
                if (isTruthy(left)) return left;
                break;
            case AND:
                if (!isTruthy(left)) return left;
                break;
            default: // unreachable
                break;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr arg : expr.arguments)
            arguments.add(evaluate(arg));

        LoxCallable function = (LoxCallable) callee;
        if (function.arity() != arguments.size())
            throw new RuntimeError(expr.paren, String.format("Function expects %d arguments, but got %d.",
                    function.arity(), arguments.size()));
        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.object);
        if (object instanceof LoxInstance)
            return ((LoxInstance) object).get(expr.name);

        throw new RuntimeError(expr.name, "Only instances have properties.");
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof LoxInstance))
            throw new RuntimeError(expr.name, "Only instances have fields.");

        Object value = evaluate(expr.value);
        ((LoxInstance) object).set(expr.name, value);

        return value;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        int distance = locals.get(expr);
        LoxClass superclass = (LoxClass) env.getAt(distance, "super");
        LoxInstance object = (LoxInstance) env.getAt(distance - 1, "this");

        LoxFunction method = superclass.findMethod(expr.method.lexeme);
        if (method == null)
            throw new RuntimeError(expr.method,
                    String.format("Undefined property '%s'.", expr.method.lexeme));
        return method.bind(object);
    }

    @Override
    public Object visitLambdaExpr(Expr.Lambda expr) {
        LoxFunction function = new LoxFunction(expr, env);
        return function;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.stmts, new Environment(env));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof LoxClass))
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
        }

        env.define(stmt.name.lexeme, null);
        if (stmt.superclass != null) {
            env = new Environment(env);
            env.define("super", superclass);
        }

        Map<String, LoxFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            LoxFunction function = new LoxFunction(method, env, method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme, function);
        }

        LoxClass klass = new LoxClass(stmt.name.lexeme, (LoxClass) superclass, methods);

        if (superclass != null)
            env = env.enclosing;
        env.assign(stmt.name, klass);

        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expr);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition)))
            execute(stmt.thenBranch);
        else if (stmt.elseBranch != null)
            execute(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null)
            value = evaluate(stmt.initializer);
        env.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (Break ignored) {
                break;
            }
        }
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new Break();
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt, env);
        env.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.expr != null)
            value = evaluate(stmt.expr);
        throw new Return(value);
    }
}

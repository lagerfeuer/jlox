package at.lagerfeuer.lox;

import at.lagerfeuer.lox.ast.Expr;
import at.lagerfeuer.lox.ast.Stmt;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    public LoxFunction(Stmt.Function declaration, Environment closure) {
        this(declaration, closure, false);
    }

    public LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    public LoxFunction(Expr.Lambda lambda, Environment closure) {
        this.declaration = new Stmt.Function(null, lambda.parameters, lambda.body);
        this.closure = closure;
        this.isInitializer = false;
    }

    @Override
    public int arity() {
        return declaration.parameters.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.parameters.size(); i++) {
            environment.define(declaration.parameters.get(i).lexeme,
                    arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return ret) {
            if (isInitializer)
                return closure.getAt(0, "this");
            return ret.value;
        }

        if (isInitializer)
            return closure.getAt(0, "this");
        return null;
    }

    @Override
    public String toString() {
        return (declaration.name != null) ? "<function " + declaration.name.lexeme + ">" : "<anonymous function>";
    }

    public LoxFunction bind(LoxInstance instance) {
        Environment env = new Environment(closure);
        env.define("this", instance);
        return new LoxFunction(declaration, env, isInitializer);
    }
}

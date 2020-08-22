package at.lagerfeuer.lox;

import at.lagerfeuer.lox.ast.Expr;
import at.lagerfeuer.lox.ast.Stmt;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    public List<Qualifier> getQualifiers() {
        return qualifiers;
    }

    private final List<Qualifier> qualifiers;

    public LoxFunction(Stmt.Function declaration, Environment closure) {
        this(declaration, closure, false, new ArrayList<>());
    }

    public LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this(declaration, closure, isInitializer, new ArrayList<>());
    }

    public LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer, List<Qualifier> qualifiers) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
        this.qualifiers = qualifiers;
    }

    public LoxFunction(Expr.Lambda lambda, Environment closure) {
        this.declaration = new Stmt.Function(null, lambda.parameters, lambda.body, new ArrayList<>());
        this.closure = closure;
        this.isInitializer = false;
        this.qualifiers = null;
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
        String qs = qualifiers.stream().map(Enum::toString).collect(Collectors.joining());
        return (declaration.name != null)
                ? String.format("<function %s :: %s>", declaration.name.lexeme, qs)
                : "<anonymous function>";
    }

    public LoxFunction bind(LoxInstance instance) {
        Environment env = new Environment(closure);
        env.define("this", instance);
        return new LoxFunction(declaration, env, isInitializer);
    }
}

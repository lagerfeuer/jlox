package at.lagerfeuer.lox;

import java.util.List;
import java.util.Map;

public class LoxClass extends LoxInstance implements LoxCallable {
    final String name;
    private final Map<String, LoxFunction> methods;

    LoxClass(String name, Map<String, LoxFunction> methods) {
        super(null);
        this.name = name;
        this.methods = methods;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer == null)
            return 0;
        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    public LoxFunction findMethod(String name) {
        if (methods.containsKey(name))
            return methods.get(name);
        return null;
    }

    @Override
    Object get(Token name) {
        LoxFunction method = findMethod(name.lexeme);
        if (method != null)
            return (method.getQualifiers().contains(Qualifier.STATIC)) ? method : method.bind(this);

        throw new RuntimeError(name, String.format("Undefined property '%s'.", name.lexeme));
    }
}

package at.lagerfeuer.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();
    final Environment enclosing;

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object value) {
        // TODO overwriting an existing value with 'define' should throw a RuntimeError
        // TODO save tokens so we can reference them "Variable <name> already defined here: file.lox:123"
        values.put(name, value);
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme))
            values.put(name.lexeme, value);
        else if (enclosing != null)
            enclosing.assign(name, value);
        else
            throw new RuntimeError(name,
                    String.format("Undefined variable '%s'.", name.lexeme));
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme))
            return values.get(name.lexeme);

        if (enclosing != null)
            return enclosing.get(name);

        throw new RuntimeError(name,
                String.format("Undefined variable '%s'.", name.lexeme));
    }

    Environment ancestor(int distance) {
        Environment env = this;
        for (int i = 0; i < distance; i++) {
            assert env != null;
            env = env.enclosing;
        }
        return env;
    }

    void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }

    Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (enclosing != null)
            builder.append(enclosing.toString());
        for (String key : values.keySet()) {
            String value = values.get(key) != null ? values.get(key).toString() : "null";
            builder.append(String.format("%s:\t%s\n", key, value));
        }
        return builder.toString();
    }
}

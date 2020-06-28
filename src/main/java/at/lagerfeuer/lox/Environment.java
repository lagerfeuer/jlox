package at.lagerfeuer.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    void define(String name, Object value) {
        // TODO overwriting an existing value with 'define' should throw a RuntimeError
        // TODO save tokens so we can reference them "Variable <name> already defined here: file.lox:123"
        values.put(name, value);
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme))
            values.put(name.lexeme, value);
        else
            throw new RuntimeError(name,
                    String.format("Undefined variable '%s'.", name.lexeme));
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme))
            return values.get(name.lexeme);

        throw new RuntimeError(name,
                String.format("Undefined variable '%s'.", name.lexeme));
    }
}

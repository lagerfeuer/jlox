package at.lagerfeuer.lox;

import java.util.Objects;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;
    public final String file;

    /**
     * Constructor for tokens.
     *
     * @param type    Token type
     * @param lexeme  Lexeme
     * @param literal Literal
     * @param file    Filename of the Lox source file
     * @param line    Line number where token is located
     */
    public Token(TokenType type, String lexeme, Object literal, String file, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.file = file;
    }

    /**
     * Shorthand constructor for REPL tokens omitting line number and filename.
     *
     * @param type    Token type
     * @param lexeme  Lexeme
     * @param literal Literal
     */
    public Token(TokenType type, String lexeme, Object literal) {
        this(type, lexeme, literal, "<stdin>", -1);
    }

    @Override
    public String toString() {
        return String.format("<Token> %s %s %s", type, lexeme, literal);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass())
            return false;

        Token other = (Token) obj;
        return (this.type == other.type)
                && (Objects.equals(this.lexeme, other.lexeme))
                && (Objects.equals(this.literal, other.literal));
    }
}

package at.lagerfeuer.lox;

import java.util.Objects;

public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;
    public final String filename;

    /**
     * Constructor for tokens.
     *
     * @param type    Token type
     * @param lexeme  Lexeme
     * @param literal Literal
     * @param filename    Filename of the Lox source filename
     * @param line    Line number where token is located
     */
    public Token(TokenType type, String lexeme, Object literal, String filename, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.filename = filename;
    }

    /**
     * Shorthand constructor for REPL tokens omitting line number and filenamename.
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

package at.lagerfeuer.lox.token;

public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;
    final String file;

    /**
     * Constructor for tokens.
     * @param type Token type
     * @param lexeme Lexeme
     * @param literal Literal
     * @param line Line number where token is located
     * @param file Filename of the Lox source file
     */
    public Token(TokenType type, String lexeme, Object literal, int line, String file) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.file = file;
    }

    /**
     * Shorthand constructor for REPL tokens omitting line number and filename.
     * @param type Token type
     * @param lexeme Lexeme
     * @param literal Literal
     */
    public Token(TokenType type, String lexeme, Object literal) {
        this(type, lexeme, literal, -1, "<stdin>");
    }

    public String toString() {
        return String.format("<Token> %s %s %s", type, lexeme, literal);
    }
}

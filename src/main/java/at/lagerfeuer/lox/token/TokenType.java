package at.lagerfeuer.lox.token;

public enum TokenType {
    // Literals
    IDENTIFIER,
    STRING,
    NUMBER,
    // Single char
    PLUS,
    MINUS,
    STAR,
    SLASH,
    BANG,
    EQUAL,
    DOT, COMMA, COLON, SEMICOLON,
    // Comparison
    EQUAL_EQUAL, BANG_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    // Parenthesis
    LPAREN, RPAREN,
    LBRACKET, RBRACKET,
    LBRACE, RBRACE,
    // Keywords
    NIL,
    AND, OR,
    TRUE, FALSE,
    CLASS, VAR, FUN,
    IF, ELSE,
    FOR, WHILE,
    PRINT,
    RETURN,
    SUPER, THIS,

    // EOF
    EOF
}

package at.lagerfeuer.lox;

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
    QUESTION,
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
    FOR, WHILE, BREAK,
    RETURN,
    SUPER, THIS,
    STATIC,
    // EOF
    EOF
}

package at.lagerfeuer.lox.lexer;

import at.lagerfeuer.lox.Lox;
import at.lagerfeuer.lox.token.Token;
import at.lagerfeuer.lox.token.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static at.lagerfeuer.lox.token.TokenType.*;

public class Lexer {
    private final String SOURCE;
    private final List<Token> TOKENS = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private String filename;
    private int line = 1;

    private static final Map<String, TokenType> KEYWORDS = new HashMap<>() {{
        put("nil", NIL);
        put("and", AND);
        put("or", OR);
        put("true", TRUE);
        put("false", FALSE);
        put("class", CLASS);
        put("var", VAR);
        put("fun", FUN);
        put("if", IF);
        put("else", ELSE);
        put("for", FOR);
        put("while", WHILE);
        put("print", PRINT); // TODO make print a stdlib function, not a statement
        put("return", RETURN);
        put("super", SUPER);
        put("this", THIS);
    }};

    public Lexer(String source) {
        this(source, "<stdin>");
    }

    public Lexer(String source, String filename) {
        this.SOURCE = source;
        this.filename = filename;
    }

    private char advance() {
        return SOURCE.charAt(current++);
    }

    private char peek() {
        if (isEOF())
            return '\0';
        return SOURCE.charAt(current);
    }

    private char peekNext() {
        if (isNextEOF())
            return '\0';
        return SOURCE.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isEOF())
            return false;
        if (SOURCE.charAt(current) != expected)
            return false;
        current++;
        return true;
    }

    /**
     * Returns whether the end of the SOURCE code been reached.
     *
     * @return true if end of the SOURCE has been reached, false otherwise.
     */
    private boolean isEOF() {
        return current >= SOURCE.length();
    }

    /**
     * Returns whether the next position on the SOURCE is the end of the SOURCE.
     *
     * @return true if the next char is the end of the SOURCE, false otherwise.
     */
    private boolean isNextEOF() {
        return (current + 1) >= SOURCE.length();
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = SOURCE.substring(start, current);
        TOKENS.add(new Token(type, text, literal, line, filename));
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '+':
                addToken(PLUS); break;
            case '-':
                addToken(MINUS); break;
            case '*':
                addToken(STAR); break;
            case '/':
                if (peek() == '/' || peek() == '*')
                    comment();
                else
                    addToken(SLASH);
                break;
            case '.':
                addToken(DOT); break;
            case ',':
                addToken(COMMA); break;
            case ':':
                addToken(COLON); break;
            case ';':
                addToken(SEMICOLON); break;
            case '(':
                addToken(LPAREN); break;
            case ')':
                addToken(RPAREN); break;
            case '[':
                addToken(LBRACKET); break;
            case ']':
                addToken(RBRACKET); break;
            case '{':
                addToken(LBRACE); break;
            case '}':
                addToken(RBRACE); break;
            case '!':
                addToken((match('=') ? BANG_EQUAL : BANG)); break;
            case '=':
                addToken((match('=') ? EQUAL_EQUAL : EQUAL)); break;
            case '<':
                addToken((match('=') ? LESS_EQUAL : LESS)); break;
            case '>':
                addToken((match('=') ? GREATER_EQUAL : GREATER)); break;
            case '"':
                string(); break;
            // whitespaces
            case '\n':
                line++;
            case ' ':
            case '\t':
            case '\r':
                break;
            // default
            default:
                if (isDigit(c))
                    number();
                else if (isAlpha(c))
                    identifierOrKeyword();
                else
                    Lox.error(filename, line,
                            "Unexpected char: " + c);
                break;
        }
    }

    private boolean isDigit(char c) { return c >= '0' && c <= '9'; }

    private boolean isAlpha(char c) { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_'); }

    private boolean isAlphaNumeric(char c) { return isAlpha(c) || isDigit(c); }

    /**
     * Parse string literal.
     */
    private void string() {
        while (peek() != '"' && !isEOF()) {
            if (peek() == '\n')
                line++;
            advance();
        }

        // unterminated string
        if (isEOF()) {
            Lox.error(filename, line, "Unterminated string");
            return;
        }

        advance(); // consume closing "
        String literal = SOURCE.substring(start + 1, current - 1);
        // TODO unescape escape sequences like '\n' here
        addToken(STRING, literal);
    }


    /**
     * Parse number literal.
     */
    private void number() {
        // pure integer
        while (isDigit(peek()))
            advance();
        // float literal
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek()))
                advance();
        }

        String literal = SOURCE.substring(start, current);
        addToken(NUMBER, Double.parseDouble(literal));
    }

    /**
     * Parse identifier or keyword.
     */
    private void identifierOrKeyword() {
        while (isAlphaNumeric(peek()))
            advance();

        String text = SOURCE.substring(start, current);
        TokenType type = KEYWORDS.get(text);

        if (type == null)
            type = IDENTIFIER;
        addToken(type);
    }

    /**
     * Consume single and multi line comments.
     */
    private void comment() {
        if (match('/')) {
            while (peek() != '\n' && !isEOF())
                advance();
        } else {
            while(!(peek() == '*' && peekNext() == '/') && !isEOF()) {
                advance();
            }
            advance(); // eat '*' in '*/'
            advance(); // eat '/' in '*/'
        }

    }

    /**
     * Scan all TOKENS and return them as list.
     *
     * @return a list of all TOKENS plus a trailing EOF token
     */
    public List<Token> scanTokens() {
        while (!isEOF()) {
            start = current;
            scanToken();
        }

        TOKENS.add(new Token(EOF, "", null, line, filename));
        return TOKENS;
    }
}

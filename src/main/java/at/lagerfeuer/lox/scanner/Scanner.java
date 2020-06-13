package at.lagerfeuer.lox.scanner;

import at.lagerfeuer.lox.Lox;
import at.lagerfeuer.lox.token.Token;
import at.lagerfeuer.lox.token.TokenType;

import java.util.ArrayList;
import java.util.List;

import static at.lagerfeuer.lox.token.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;

    private String filename;
    private int line = 1;

    public Scanner(String source) {
        this(source, "<stdin>");
    }

    public Scanner(String source, String filename) {
        this.source = source;
        this.filename = filename;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line, filename));
    }

    private boolean isEOF() {
        return current >= source.length();
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
                addToken(SLASH); break;
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
            default:
                Lox.error(filename, line,
                        "Unexpected char: " + c);
        }
    }

    public List<Token> scanTokens() {
        while (!isEOF()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line, filename));
        return tokens;
    }
}

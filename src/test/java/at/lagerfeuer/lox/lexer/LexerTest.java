package at.lagerfeuer.lox.lexer;

import at.lagerfeuer.lox.Lexer;
import at.lagerfeuer.lox.Token;
import at.lagerfeuer.lox.TokenType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static at.lagerfeuer.lox.TokenType.*;

class LexerTest {

    private TokenType[] lex(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        List<TokenType> types = new ArrayList<>();
        for (Token token : tokens)
            types.add(token.getType());

        return types.toArray(new TokenType[0]);
    }

    @Test
    void keywords() {
        String source = "nil and or true false class var fun if else for while print return super this";
        TokenType[] ref = {
                NIL, AND, OR, TRUE, FALSE, CLASS, VAR, FUN, IF, ELSE, FOR, WHILE, PRINT, RETURN, SUPER, THIS, EOF
        };
        TokenType[] types = lex(source);
        assertArrayEquals("Keyword Tokens", ref, types);
    }

    @Test
    void operators() {
        String source = "+ - * / ! =";
        TokenType[] ref = {PLUS, MINUS, STAR, SLASH, BANG, EQUAL, EOF};
        TokenType[] types = lex(source);
        assertArrayEquals("Operator Tokens", ref, types);
    }

    @Test
    void comparison() {
        String source = "!= == < > <= >=";
        TokenType[] ref = {BANG_EQUAL, EQUAL_EQUAL, LESS, GREATER, LESS_EQUAL, GREATER_EQUAL, EOF};
        TokenType[] types = lex(source);
        assertArrayEquals("Comparison Tokens", ref, types);
    }

    @Test
    void literals() {
        String source = "iAmAVariable \"string\" 123 3.1415";
        Token[] ref = {
                new Token(IDENTIFIER, "iAmAVariable", null),
                new Token(STRING, "\"string\"", "string"),
                new Token(NUMBER, "123", 123.d),
                new Token(NUMBER, "3.1415", 3.1415d),
                new Token(EOF, "", null)
        };

        Token[] tokens = new Lexer(source).scanTokens().toArray(new Token[0]);

        assertArrayEquals("Literal Tokens", ref, tokens);
    }

    @Test
    void parenthesis() {
        String source = "() [] {}";
        TokenType[] ref = {LPAREN, RPAREN, LBRACKET, RBRACKET, LBRACE, RBRACE, EOF};
        TokenType[] types = lex(source);
        assertArrayEquals("Parenthesis Tokens", ref, types);
    }

    @Test
    void comments() {
        String source = "// this is a comment\n"
                + "abc // this is a comment too\n"
                + "def\n"
                + "/* this is a\nlong\ncomment\n*/\n"
                + "ghi";
        Token[] ref = {
                new Token(IDENTIFIER, "abc", null),
                new Token(IDENTIFIER, "def", null),
                new Token(IDENTIFIER, "ghi", null),
                new Token(EOF, "", null)
        };
        Token[] tokens = new Lexer(source).scanTokens().toArray(new Token[0]);
        assertArrayEquals("Comments", ref, tokens);
    }

    @Test
    void others() {
        String source = ". : , ;";
        TokenType[] ref = {DOT, COLON, COMMA, SEMICOLON, EOF};
        TokenType[] types = lex(source);
        assertArrayEquals("Other Tokens", ref, types);
    }

    @Test
    void unexpected() {
        String source ="%";
        TokenType[] ref = {EOF};
        TokenType[] types = lex(source);
        assertArrayEquals("Unknown Token/Unexpected Char", ref, types);
    }

    @Test
    void unterminated_string() {
        String source = "\"string";
        TokenType[] ref = {EOF};
        TokenType[] types = lex(source);
        assertArrayEquals("Unterminated String", ref, types);
    }
}
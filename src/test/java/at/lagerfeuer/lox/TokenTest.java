package at.lagerfeuer.lox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenTest {

    @Test
    void toStringTest() {
        Token token = new Token(TokenType.NIL, "nil", null);
        String str = token.toString();
        assertTrue(str.startsWith("<Token>"));
    }

    @Test
    void equalsTest() {
        Token test = new Token(TokenType.NIL, "nil", null);
        Token ref = new Token(TokenType.NIL, "nil", null);
        Token[] tokens = {
                new Token(TokenType.TRUE, "nil", null),
                new Token(TokenType.NIL, "nothing", null),
                new Token(TokenType.NIL, "nil", 3),
                null
        };
        assertEquals(test, ref);
        for (Token tok : tokens) {
            assertNotEquals(test, tok);
        }
    }
}
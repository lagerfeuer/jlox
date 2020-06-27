package at.lagerfeuer.lox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest extends Interpreter {
    private Object interpret(String input) {
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.scanTokens());
        return new Interpreter().interpret(parser.parse());
    }

    @Test
    void interpret() {
        // assertThrows(RuntimeError.class, () -> interpret("1 + nil"));
        assertNull(interpret("1 + nil"));
    }

    @Test
    void stringify() {
        assertEquals("nil", Interpreter.stringify(null));
        assertEquals("1", Interpreter.stringify(1.0));
        assertEquals("java", Interpreter.stringify("java"));
    }

    @Test
    void visitBinaryExpr() {
        double ref = 4.d;
        assertEquals(ref, interpret("1 + 3"));
        assertEquals(ref, interpret("6 - 2"));
        assertEquals(ref, interpret("2 * 2"));
        assertEquals(ref, interpret("8 / 2"));

        assertNull(interpret("1 / 0")); // throws error internally

        assertEquals("test4", interpret("\"test\" + 4"));
        assertEquals("4test", interpret("4 + \"test\""));

        assertEquals(true, interpret("\"a\" < \"b\""));
        assertEquals(true, interpret("\"a\" <= \"b\""));
        assertEquals(true, interpret("\"a\" != \"b\""));
        assertEquals(false, interpret("\"a\" == \"b\""));
        assertEquals(false, interpret("\"a\" >= \"b\""));
        assertEquals(false, interpret("\"a\" > \"b\""));

        assertEquals(false, interpret("\"a\" < \"a\""));
        assertEquals(true, interpret("\"a\" <= \"a\""));
        assertEquals(false, interpret("\"a\" != \"a\""));
        assertEquals(true, interpret("\"a\" == \"a\""));
        assertEquals(true, interpret("\"a\" >= \"a\""));
        assertEquals(false, interpret("\"a\" > \"a\""));
    }

    @Test
    void visitGroupingExpr() {
        assertEquals(4.0, interpret("(1 + (1 + (1 + (1))))"));
    }

    @Test
    void visitLiteralExpr() {
        assertEquals(1.0, interpret("1"));
        assertEquals("test", interpret("\"test\""));
        assertEquals(null, interpret("nil"));
        assertEquals(true, interpret("true"));
        assertEquals(false, interpret("false"));
    }

    @Test
    void visitUnaryExpr() {
        assertEquals(true, interpret("!0"));
        assertEquals(false, interpret("!!0"));
        assertEquals(false, interpret("!1"));
        assertEquals(true, interpret("!!1"));
        assertEquals(true, interpret("!\"\""));
        assertEquals(false, interpret("!!\"\""));
        assertEquals(false, interpret("!\"test\""));
        assertEquals(true, interpret("!!\"test\""));
        assertEquals(true, interpret("!nil"));
        assertEquals(false, interpret("!!nil"));
        assertEquals(false, interpret("!true"));
        assertEquals(true, interpret("!false"));

        assertEquals(-1.0, interpret("-1"));
    }

    @Test
    void visitTernaryExpr() {
        assertEquals(1.0, interpret("true ? true ? 1 : 0 : -1"));
        assertEquals(0.0, interpret("true ? false ? 1 : 0 : -1"));
        assertEquals(-1.0, interpret("false ? false ? 1 : 0 : -1"));
    }

    @Test
    void visitCommaExpr() {
        assertEquals(null, interpret("true, \"test\", 1, nil"));
        assertEquals(1.0, interpret("true, \"test\", 1"));
        assertEquals("test", interpret("true, \"test\""));
        assertEquals(true, interpret("nil, nil, true"));
    }
}
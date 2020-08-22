package at.lagerfeuer.lox;

import at.lagerfeuer.lox.ast.Expr;
import at.lagerfeuer.lox.ast.Stmt;
import at.lagerfeuer.utils.ExitCode;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    static boolean interactive = false;

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Print this message")
                .build());

        DefaultParser parser = new DefaultParser();
        CommandLine cli = null;
        try {
            cli = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("CLI Parser Error");
            System.exit(ExitCode.USAGE);
        }
        String[] cliArgs = cli.getArgs();
        HelpFormatter help = new HelpFormatter();

        if (cli.hasOption("help")) {
            help.printHelp("jlox", options);
            System.exit(ExitCode.SUCCESS);
        }

        if (cliArgs.length > 1) {
            help.printHelp("jlox", options);
            System.exit(ExitCode.USAGE);
        } else if (cliArgs.length == 1) {
            runFile(cliArgs[0]);
        } else {
            interactive = true;
            repl();
        }

    }

    /**
     * Execute the file supplied by `path` as lox source code.
     *
     * @param path Lox source file.
     */
    private static void runFile(String path) {
        try {
            String content = Files.readString(Paths.get(path), Charset.defaultCharset());
            run(content, new File(path).getName());
            if (hadError)
                System.exit(ExitCode.DATAERR);
            if (hadRuntimeError)
                System.exit(ExitCode.SOFTWARE);

        } catch (IOException e) {
            System.err.println("Could not read file " + path);
            System.exit(ExitCode.DATAERR);
        }
    }

    /**
     * Execute Lox REPL.
     */
    private static void repl() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println(String.format("JLox v%s", "0.1"));

        try {
            for (; ; ) {
                System.out.print("> ");
                String line = reader.readLine();
                if (line == null || line.isEmpty()) // CTRL + D
                    return;
                run(line, "<stdin>");
                hadError = false;
            }
        } catch (IOException e) {
            System.err.println("Could not read from STDIN");
            System.exit(ExitCode.DATAERR);
        }
    }

    private static void run(String source, String filename) {
        Lexer lexer = new Lexer(source, filename);
        List<Token> tokens = lexer.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();

        // exit if parser error occurred.
        if (hadError)
            return;

        ResolverPass resolver = new ResolverPass(interpreter);
        resolver.resolve(stmts);

        // exit if resolver error occurred.
        if (hadError)
            return;

        if (interactive && stmts.size() == 1 && stmts.get(0) instanceof Stmt.Expression) {
            // Print the result of a single expression
            Object result = interpreter.interpret(((Stmt.Expression) stmts.get(0)).expr);
            System.out.println(Interpreter.stringify(result));
        } else {
            interpreter.interpret(stmts);
        }
    }

    public static void error(String filename, int line, String message) {
        hadError = true;
        report(filename, line, message);
    }

    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.filename, token.line, " at end", message);
        } else {
            report(token.filename, token.line, " at '" + token.lexeme + "'", message);
        }
    }

    public static void runtimeError(RuntimeError error) {
        System.err.printf("[RuntimeError] %s:%d\t%s%n",
                error.token.filename, error.token.line, error.getMessage());
        hadRuntimeError = true;
    }

    private static void report(String filename, int line, String message) {
        System.err.printf("[Error] %s:%d\t%s%n", filename, line, message);
    }

    private static void report(String filename, int line, String where, String message) {
        System.err.printf("[Error] %s:%d\t(%s)\t%s%n", filename, line, where, message);
    }
}

package at.lagerfeuer.lox;

import at.lagerfeuer.lox.scanner.Scanner;
import at.lagerfeuer.lox.token.Token;
import at.lagerfeuer.lox.utils.ExitCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    static boolean hadError = false;
    static boolean interactive = false;

    public static void main(String args[]) {
        if (args.length > 1) {
            System.out.println("Usage: jlox [file]");
            System.exit(ExitCode.USAGE);
        } else if (args.length == 1) {
            runFile(args[0]);
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
            String content = new String(Files.readAllBytes(Paths.get(path)),
                    Charset.defaultCharset());
            run(content);
            if (hadError)
                System.exit(ExitCode.DATAERR);

        } catch (IOException e) {
            System.err.println("Could not read file " + path);
            System.exit(ExitCode.DATAERR);
        }
    }

    /**
     * Execute Lox REPL.
     */
    private static void repl() {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        try {
            for (; ; ) {
                System.out.print("> ");
                run(reader.readLine());
                hadError = false;
            }
        } catch (IOException e) {
            System.err.println("Could not read from STDIN");
            System.exit(ExitCode.DATAERR);
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        for (Token token : tokens)
            System.out.println(token);
    }

    public static void error(String filename, int line, String message, Object... arguments) {
        error(filename, line, String.format(message, arguments));
    }

    public static void error(String filename, int line, String message) {
        hadError = true;
        report(line, filename, message);
    }

    private static void report(int line, String where, String message) {
        System.err.println(String.format("[Error] %s:%d\t%s", where, line, message));
    }
}

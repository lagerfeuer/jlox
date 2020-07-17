package at.lagerfeuer.tool

import java.nio.file.Paths

def defineAst(String outputDir, String baseName, List<String> types) {
    String path = Paths.get(outputDir, baseName).toString() + ".java"
    PrintWriter writer = new PrintWriter(path, "UTF-8")

    // Opening
    writer.println("package at.lagerfeuer.lox.ast;")
    writer.println()
    writer.println("import java.util.List;")
    writer.println("import at.lagerfeuer.lox.*;")
    writer.println()
    writer.println("// Generated source code")
    writer.println(String.format("public abstract class %s {", baseName))

    defineVisitor(writer, baseName, types)

    // Content
    for (String type : types) {
        String[] parts = type.split(":")
        String className = parts[0].trim()
        String fields = parts[1].trim()
        defineType(writer, baseName, className, fields)
    }

    // base accept() method
    writer.println()
    writer.println("  abstract public <R> R accept(Visitor<R> visitor);")

    // Closing
    writer.println("}")
    writer.close()
}

def defineVisitor(
        PrintWriter writer, String baseName, List<String> types) {
    writer.println("  public interface Visitor<R> {")
    for (String type : types) {
        String typeName = type.split(':')[0].trim()
        writer.println(String.format("    R visit%s%s(%s %s);", typeName, baseName, typeName, baseName.toLowerCase()))
    }
    writer.println("  }")
}

def defineType(
        PrintWriter writer, String baseName, String className, String fieldList) {
    writer.println(String.format("  public static class %s extends %s {", className, baseName))
    // Constructor
    writer.println(String.format("    public %s (%s) {", className, fieldList))
    String[] fields = fieldList.split(", ")
    for (String field : fields) {
        String name = field.split(" ")[1]
        writer.println(String.format("      this.%s = %s;", name, name))
    }
    writer.println("    }")

    // Visitor pattern
    writer.println()
    writer.println("    @Override")
    writer.println("    public <R> R accept(Visitor<R> visitor) {")
    writer.println(String.format("      return visitor.visit%s%s(this);", className, baseName))
    writer.println("    }")

    // Fields
    writer.println()
    for (String field : fields) {
        writer.println(String.format("   public final %s;", field))
    }

    writer.println("  }")
}


// Main
def outputDir = new File("src/main/java/at/lagerfeuer/lox/ast/")
if (!outputDir.exists())
    outputDir.mkdirs()
def out = outputDir.toString()

// Expression
List<String> expr = Arrays.asList(
        "Assign : Token name, Expr value",
        "Binary : Expr left, Token operator, Expr right",
        "Grouping : Expr expr",
        "Literal : Object value",
        "Unary : Token operator, Expr right",
        "Variable : Token name",
        "Ternary : Expr condition, Expr thenBranch, Expr elseBranch",
        "Comma : List<Expr> exprs",
        "Logical : Expr left, Token operator, Expr right"
)
defineAst(out, "Expr", expr)

// Statement
List<String> stmt = Arrays.asList(
        "Block : List<Stmt> stmts",
        "Expression : Expr expr",
        "If : Expr condition, Stmt thenBranch, Stmt elseBranch",
        "Print : Expr expr",
        "Var : Token name, Expr initializer",
        "While : Expr condition, Stmt body",
        "Break : Token token"
)
defineAst(out, "Stmt", stmt)

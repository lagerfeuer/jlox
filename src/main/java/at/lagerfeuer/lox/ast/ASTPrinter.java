package at.lagerfeuer.lox.ast;

import java.util.List;

public class ASTPrinter implements Expr.Visitor<String>, Stmt.Visitor<Void> {

    public void print(List<Stmt> stmts) {
        for (Stmt stmt : stmts)
            stmt.accept(this);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return String.format("%s = %s", expr.name.lexeme,
                expr.value.accept(this));
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expr);
    }


    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(expr.condition.accept(this))
                .append(" ? ").append(expr.trueBranch.accept(this))
                .append(" : ").append(expr.falseBranch.accept(this))
                .append(")");
        return builder.toString();
    }

    @Override
    public String visitCommaExpr(Expr.Comma expr) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (Expr c_expr : expr.exprs) {
            builder.append("(").append(c_expr.accept(this)).append("), ");
        }
        builder.setLength(builder.length() - 2);
        builder.append(")");
        return builder.toString();
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        System.out.println("{");
        for (Stmt s : stmt.stmts) {
            s.accept(this);
        }
        System.out.println("}");
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        System.out.println(stmt.expr.accept(this));
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        System.out.println("print " + stmt.expr.accept(this));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        System.out.println("var " + stmt.name.lexeme + " = " +
                stmt.initializer.accept(this));
        return null;
    }
}

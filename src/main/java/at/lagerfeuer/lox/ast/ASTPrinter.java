package at.lagerfeuer.lox.ast;

public class ASTPrinter implements Expr.Visitor<String> {

    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.getLexeme(), expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }


    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.getLexeme(), expr.right);
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
}

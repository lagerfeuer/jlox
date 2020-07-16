package at.lagerfeuer.lox.ast;

import java.util.List;
import at.lagerfeuer.lox.*;

// Generated source code
public abstract class Expr {
  public interface Visitor<R> {
    R visitAssignExpr(Assign expr);
    R visitBinaryExpr(Binary expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitUnaryExpr(Unary expr);
    R visitVariableExpr(Variable expr);
    R visitTernaryExpr(Ternary expr);
    R visitCommaExpr(Comma expr);
    R visitLogicalExpr(Logical expr);
  }
  public static class Assign extends Expr {
    public Assign (Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }

   public final Token name;
   public final Expr value;
  }
  public static class Binary extends Expr {
    public Binary (Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

   public final Expr left;
   public final Token operator;
   public final Expr right;
  }
  public static class Grouping extends Expr {
    public Grouping (Expr expr) {
      this.expr = expr;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }

   public final Expr expr;
  }
  public static class Literal extends Expr {
    public Literal (Object value) {
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }

   public final Object value;
  }
  public static class Unary extends Expr {
    public Unary (Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

   public final Token operator;
   public final Expr right;
  }
  public static class Variable extends Expr {
    public Variable (Token name) {
      this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }

   public final Token name;
  }
  public static class Ternary extends Expr {
    public Ternary (Expr condition, Expr thenBranch, Expr elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitTernaryExpr(this);
    }

   public final Expr condition;
   public final Expr thenBranch;
   public final Expr elseBranch;
  }
  public static class Comma extends Expr {
    public Comma (List<Expr> exprs) {
      this.exprs = exprs;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitCommaExpr(this);
    }

   public final List<Expr> exprs;
  }
  public static class Logical extends Expr {
    public Logical (Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpr(this);
    }

   public final Expr left;
   public final Token operator;
   public final Expr right;
  }

  abstract public <R> R accept(Visitor<R> visitor);
}

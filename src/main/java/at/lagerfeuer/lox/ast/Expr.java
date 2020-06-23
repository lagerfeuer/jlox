package at.lagerfeuer.lox.ast;

import java.util.List;
import at.lagerfeuer.lox.*;

// Generated source code
public abstract class Expr {
  interface Visitor<R> {
    R visitBinaryExpr(Binary expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitUnaryExpr(Unary expr);
  }
  public static class Binary extends Expr {
    public Binary (Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

   public final Expr left;
   public final Token operator;
   public final Expr right;
  }
  public static class Grouping extends Expr {
    public Grouping (Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }

   public final Expr expression;
  }
  public static class Literal extends Expr {
    public Literal (Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
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
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

   public final Token operator;
   public final Expr right;
  }

  abstract <R> R accept(Visitor<R> visitor);
}

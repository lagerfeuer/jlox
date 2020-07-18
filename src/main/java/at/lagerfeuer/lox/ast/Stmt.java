package at.lagerfeuer.lox.ast;

import java.util.List;
import at.lagerfeuer.lox.*;

// Generated source code
public abstract class Stmt {
  public interface Visitor<R> {
    R visitBlockStmt(Block stmt);
    R visitExpressionStmt(Expression stmt);
    R visitIfStmt(If stmt);
    R visitVarStmt(Var stmt);
    R visitWhileStmt(While stmt);
    R visitBreakStmt(Break stmt);
  }
  public static class Block extends Stmt {
    public Block (List<Stmt> stmts) {
      this.stmts = stmts;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

   public final List<Stmt> stmts;
  }
  public static class Expression extends Stmt {
    public Expression (Expr expr) {
      this.expr = expr;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

   public final Expr expr;
  }
  public static class If extends Stmt {
    public If (Expr condition, Stmt thenBranch, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }

   public final Expr condition;
   public final Stmt thenBranch;
   public final Stmt elseBranch;
  }
  public static class Var extends Stmt {
    public Var (Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

   public final Token name;
   public final Expr initializer;
  }
  public static class While extends Stmt {
    public While (Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }

   public final Expr condition;
   public final Stmt body;
  }
  public static class Break extends Stmt {
    public Break (Token token) {
      this.token = token;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBreakStmt(this);
    }

   public final Token token;
  }

  abstract public <R> R accept(Visitor<R> visitor);
}

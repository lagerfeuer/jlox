package at.lagerfeuer.lox.ast;

import java.util.List;
import at.lagerfeuer.lox.*;

// Generated source code
public abstract class Stmt {
  public interface Visitor<R> {
    R visitBreakStmt(Break stmt);
    R visitBlockStmt(Block stmt);
    R visitClassStmt(Class stmt);
    R visitExpressionStmt(Expression stmt);
    R visitFunctionStmt(Function stmt);
    R visitIfStmt(If stmt);
    R visitReturnStmt(Return stmt);
    R visitVarStmt(Var stmt);
    R visitWhileStmt(While stmt);
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

  public static class Class extends Stmt {
    public Class (Token name, Expr.Variable superclass, List<Stmt.Function> methods) {
      this.name = name;
      this.superclass = superclass;
      this.methods = methods;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }

    public final Token name;
    public final Expr.Variable superclass;
    public final List<Stmt.Function> methods;
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

  public static class Function extends Stmt {
    public Function (Token name, List<Token> parameters, List<Stmt> body, List<Qualifier> qualifiers) {
      this.name = name;
      this.parameters = parameters;
      this.body = body;
      this.qualifiers = qualifiers;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }

    public final Token name;
    public final List<Token> parameters;
    public final List<Stmt> body;
    public final List<Qualifier> qualifiers;
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

  public static class Return extends Stmt {
    public Return (Token keyword, Expr expr) {
      this.keyword = keyword;
      this.expr = expr;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStmt(this);
    }

    public final Token keyword;
    public final Expr expr;
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


  abstract public <R> R accept(Visitor<R> visitor);
}

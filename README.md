# jlox

Java implementation of Lox from the book [Crafting Interpreters](https://craftinginterpreters.com).

## 

## Changes compared to the book
* Files are split into sub-packages
* _Scanner_ is called _Lexer_

### Generate AST files
```sh
mvn exec:java -Dexec.mainClass="at.lagerfeuer.tool.GenerateAst" -Dexec.args="src/main/java/at/lagerfeuer/lox/parser/ast/"
```


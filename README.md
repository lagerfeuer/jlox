# jlox

Java implementation of Lox from the book [Crafting Interpreters](https://craftinginterpreters.com).

## Changes compared to the book
* _Scanner_ is called _Lexer_
* **Generate AST** is written in Groovy and executed during the `generate-sources` stage
* `print` is a builtin function, not a statement
* _Resolver_ is called _ResolverPass_ to indicate it's a pass


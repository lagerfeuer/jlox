# jlox

Java implementation of Lox from the book [Crafting Interpreters](https://craftinginterpreters.com).

## Changes compared to the book
* Files are split into sub-packages
* _Scanner_ is called _Lexer_
* **Generate AST** is written in Groovy and executed during the `generate-sources` stage


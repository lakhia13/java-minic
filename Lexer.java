public class Lexer {
    private static final char EOF = 0;

    private Parser yyparser;
    private java.io.Reader reader;
    public int lineno;
    private int column;
    public int colReturn = 1;

    // Double buffering with size 10
    private char[] buffer1;
    private char[] buffer2;
    private int currentBuffer;          // 1 or 2 indicating which buffer
    private int currentPos;             // Position in current buffer
    private int endPos1, endPos2;       // End positions for each buffer
    private static final int SIZE = 10;  // Buffer size must be 10

    public Lexer(java.io.Reader reader, Parser yyparser) throws Exception {
        this.reader = reader;
        this.yyparser = yyparser;
        this.lineno = 1;
        this.column = 1;
        this.prevColumn = 1;  // Initialize prevColumn

        // Initialize buffers
        buffer1 = new char[SIZE];
        buffer2 = new char[SIZE];
        currentBuffer = 1;
        currentPos = 0;

        // Fill first buffer
        endPos1 = reader.read(buffer1, 0, SIZE);
        endPos2 = 0;
    }

    private int prevColumn;  // Add field to track previous column

    private char NextChar() throws Exception {
        char c;

        // Save previous state
        prevColumn = column;

        // Handle buffer 1
        if (currentBuffer == 1) {
            if (currentPos >= endPos1) {
                if (endPos1 < SIZE) return EOF;  // End of file reached
                // Need to switch to buffer 2
                endPos2 = reader.read(buffer2, 0, SIZE);
                currentBuffer = 2;
                currentPos = 0;
                if (endPos2 <= 0) return EOF;
                c = buffer2[currentPos++];
            } else {
                c = buffer1[currentPos++];
            }
        }
        // Handle buffer 2
        else {
            if (currentPos >= endPos2) {
                if (endPos2 < SIZE) return EOF;  // End of file reached
                // Need to switch to buffer 1
                endPos1 = reader.read(buffer1, 0, SIZE);
                currentBuffer = 1;
                currentPos = 0;
                if (endPos1 <= 0) return EOF;
                c = buffer1[currentPos++];
            } else {
                c = buffer2[currentPos++];
            }
        }

        if (c == '\n') {
            lineno++;
            column = 1;
        } else {
            column++;
        }
        return c;
    }

    private char getCurrentChar() {
        if (currentBuffer == 1) {
            return currentPos > 0 ? buffer1[currentPos - 1] : '\0';
        } else {
            return currentPos > 0 ? buffer2[currentPos - 1] : '\0';
        }
    }

    private void Retract()  throws Exception{
        char c;
        if (currentPos > 0) {
            currentPos--;
            if (currentBuffer == 1) {
                if (currentPos >= endPos1) {
                    if (endPos1 < SIZE) return;  // End of file reached
                    // Need to switch to buffer 2
                    endPos2 = reader.read(buffer2, 0, SIZE);
                    currentBuffer = 2;
                    currentPos = 0;
                    if (endPos2 <= 0) return;
                    c = buffer2[currentPos];
                } else {
                    c = buffer1[currentPos];
                }
            }
            // Handle buffer 2
            else {
                if (currentPos >= endPos2) {
                    if (endPos2 < SIZE) return;  // End of file reached
                    // Need to switch to buffer 1
                    endPos1 = reader.read(buffer1, 0, SIZE);
                    currentBuffer = 1;
                    currentPos = 0;
                    if (endPos1 <= 0) return;
                    c = buffer1[currentPos];
                } else {
                    c = buffer2[currentPos];
                }
            }
            if (c == '\n') {
                lineno--;
            }
            column = prevColumn;
        }
    }

    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // Main lexical analysis function
    public int yylex() throws Exception {
        StringBuilder lexeme = new StringBuilder();
        int state = 0;
        char c;

        while (true) {
            switch (state) {
                case 0:  // Initial state
                    // Skip whitespace and track newlines
                    colReturn = column;
                    do {
                        c = NextChar();
                    } while (c == ' ' || c == '\t' || c == '\n' || c == '\r');
//                    if (c != EOF) {
//                        column = 1;  // Set column to 1 for new token
//                    }

                    if (c == EOF) return 0;

                    // Number
                    if (isDigit(c)) {
//                        colReturn = column;
                        state = 8;
                        lexeme.append(c);
                        continue;
                    }

                    // Identifier or keyword
                    if (isLetter(c)) {
//                        colReturn = column;
                        state = 9;
                        lexeme.append(c);
                        continue;
                    }
                    colReturn = column;

                    // Handle operators and punctuation
                    switch (c) {
                        case '+':
                        case '*':
                        case '/':
                            lexeme.append(c);
                            yyparser.yylval = new ParserVal((Object) lexeme.toString());
                            return Parser.OP;

                        case '-':
                            lexeme.append(c);
                            char nextChar = NextChar();
                            if (nextChar == '>') {
                                lexeme.append(nextChar);
                                yyparser.yylval = new ParserVal((Object) lexeme.toString());
                                return Parser.ASSIGN;
                            } else {
                                Retract();
                                yyparser.yylval = new ParserVal((Object) lexeme.toString());
                                return Parser.OP;
                            }

                        case '<':
                            lexeme.append(c);
                            char next = NextChar();
                            if (next == '=') {
                                lexeme.append(next);
                                yyparser.yylval = new ParserVal((Object) lexeme.toString());
                                return Parser.RELOP;
                            } else if (next == '>') {
                                lexeme.append(next);
                                yyparser.yylval = new ParserVal((Object) lexeme.toString());
                                return Parser.RELOP;
                            } else if (next == '-') {
                                lexeme.append(next);
                                yyparser.yylval = new ParserVal((Object) lexeme.toString());
                                return Parser.ASSIGN;
                            } else {
                                Retract();
                                yyparser.yylval = new ParserVal((Object) lexeme.toString());
                                return Parser.RELOP;
                            }

                        case '>':
                            lexeme.append(c);
                            next = NextChar();
                            if (next == '=') {
                                lexeme.append(next);
                            } else {
                                Retract();
                            }
                            yyparser.yylval = new ParserVal((Object) lexeme.toString());
                            return Parser.RELOP;

                        case '=':
                            lexeme.append(c);
                            yyparser.yylval = new ParserVal((Object) lexeme.toString());
                            return Parser.ASSIGN;

                        case '(':
                        case ')':
                        case '{':
                        case '}': {
                            lexeme.append(c);
                            yyparser.yylval = new ParserVal((Object) lexeme.toString());
                            return switch (c) {
                                case '(' -> Parser.LPAREN;
                                case ')' -> Parser.RPAREN;
                                case '{' -> Parser.BEGIN;
                                case '}' -> Parser.END;
                                default -> -1;
                            };
                        }

                        case ';':
                            yyparser.yylval = new ParserVal((Object) ";");
                            return Parser.SEMI;

                        case ',':
                            yyparser.yylval = new ParserVal((Object) ",");
                            return Parser.COMMA;

                        default:
                            return -1;  // Error
                    }

                case 8:  // Number state
                    c = NextChar();
                    if (isDigit(c)) {
                        lexeme.append(c);
                        continue;
                    }
                    Retract();
                    yyparser.yylval = new ParserVal((Object) lexeme.toString());
                    return Parser.NUM;

                case 9:  // Identifier or keyword state
                    c = NextChar();
                    if (isLetter(c) || isDigit(c)) {
                        lexeme.append(c);
                        continue;
                    }
                    Retract();
                    String value = lexeme.toString();
                    yyparser.yylval = new ParserVal((Object) value);

                    return switch (value) {
                        case "int" -> Parser.INT;
                        case "print" -> Parser.PRINT;
                        case "if" -> Parser.IF;
                        case "else" -> Parser.ELSE;
                        case "while" -> Parser.WHILE;
                        case "void" -> Parser.VOID;
                        default -> Parser.ID;
                    };
            }
        }
    }
}
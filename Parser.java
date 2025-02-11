public class Parser {
    public static final int OP         = 10;    // +  -  *  /
    public static final int RELOP      = 11;    // <  >  <=  >=  ...
    public static final int LPAREN     = 12;    // (
    public static final int RPAREN     = 13;    // )
    public static final int SEMI       = 14;    // ;
    public static final int COMMA      = 15;    // ,
    public static final int INT        = 16;    // int
    public static final int NUM        = 17;    // number
    public static final int ID         = 18;    // identifier
    public static final int PRINT      = 19;    // print
    public static final int VOID       = 20;    // void
    public static final int IF         = 21;    // if
    public static final int ELSE       = 22;    // else
    public static final int WHILE      = 23;    // while
    public static final int BEGIN = 24;    // {
    public static final int END = 25;    // }
    public static final int ASSIGN     = 26;    // <-

    Compiler compiler;
    Lexer lexer;
    public ParserVal yylval;

    public Parser(java.io.Reader r, Compiler compiler) throws Exception {
        this.compiler = compiler;
        this.lexer = new Lexer(r, this);
    }

    public int yyparse() throws Exception {
        while (true) {
            int token = lexer.yylex();
            
            if (token == 0) {
                System.out.println("Success!");
                return 0;
            }
            if (token == -1) {
                System.out.println("Error! There is a lexical error at " + lexer.lineno + ":" + lexer.colReturn + ".");
                return -1;
            }

            String tokenname = "";
            switch (token) {
                case OP: tokenname = "OP"; break;
                case RELOP: tokenname = "RELOP"; break;
                case LPAREN: tokenname = "LPAREN"; break;
                case RPAREN: tokenname = "RPAREN"; break;
                case SEMI: tokenname = "SEMI"; break;
                case COMMA: tokenname = "COMMA"; break;
                case INT: tokenname = "INT"; break;
                case NUM: tokenname = "NUM"; break;
                case ID: tokenname = "ID"; break;
                case PRINT: tokenname = "PRINT"; break;
                case VOID: tokenname = "VOID"; break;
                case IF: tokenname = "IF"; break;
                case ELSE: tokenname = "ELSE"; break;
                case WHILE: tokenname = "WHILE"; break;
                case BEGIN: tokenname = "BEGIN"; break;
                case END: tokenname = "END"; break;
                case ASSIGN: tokenname = "ASSIGN"; break;
            }
            System.out.println("<" + tokenname + ", token-attr:\"" + yylval.obj + "\", " + lexer.lineno + ":" + lexer.colReturn + ">");
        }
    }
}
import java.math.BigInteger;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Expr parseExpr() {
        Expr expr = new Expr();
        int change = 0;
        if (lexer.peek().equals("+") || lexer.peek().equals("-")) {
            if (lexer.peek().equals("-")) {
                change = 1;   //标记项之前的符号
            }
            lexer.next();
        }
        Term term = parseTerm();
        if (change == 1) {
            term.ChangeOp();
        }
        expr.addTerm(term);
        while (lexer.peek().equals("+") || lexer.peek().equals("-")) {  //项与项之间以+分隔
            change = 0;
            if (lexer.peek().equals("-")) {
                change = 1;
            }
            lexer.next();
            term = parseTerm();
            if (change == 1) {
                term.ChangeOp();
            }
            expr.addTerm(term);
        }
        return expr;
    }

    public Term parseTerm() {
        Term term = new Term();
        if (lexer.peek().equals("-") || lexer.peek().equals("+")) {
            if (lexer.peek().equals("-")) {
                term.ChangeOp();
            }
            lexer.next();
        }

        term.addFactor(parseFactor());

        while (lexer.peek().equals("*")) {  //因子与因子之间以*分隔
            lexer.next();
            term.addFactor(parseFactor());
        }
        return term;
    }

    public Factor parseFactor() {
        if (lexer.peek().equals("(")) {   //表达式因子
            lexer.next(); //吃掉 “(”
            Expr expr = parseExpr();  //递归 （）内为表达式
            if (lexer.peek().equals(")")) {
                lexer.next();
            }
            BigInteger num = new BigInteger("1");
            if (lexer.peek().equals("^")) {
                lexer.next();
                if (lexer.peek().equals("+")) {
                    lexer.next();
                }
                num = new BigInteger(lexer.peek());
                lexer.next();
            }
            return new ExprFac(expr, num);
        } else if (lexer.peek().equals("x")) {     //变量因子
            lexer.next();
            String exp;
            if (lexer.peek().equals("^")) {
                lexer.next();
                if (lexer.peek().equals("+")) {
                    lexer.next();
                }
                exp = lexer.peek();
                lexer.next();
            } else {
                exp = "1";
            }
            return new VarFac(exp);
        } else {      //常数因子
            char op = '+';
            if (lexer.peek().equals("-") || lexer.peek().equals("+")) {
                if (lexer.peek().equals("-")) {
                    op = '-';
                }
                lexer.next();
            }
            BigInteger num = new BigInteger(lexer.peek());
            lexer.next();
            return new NumFac(op, num);
        }
    }
}

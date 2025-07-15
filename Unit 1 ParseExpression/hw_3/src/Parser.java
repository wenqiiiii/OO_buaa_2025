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
        if (lexer.peek().equals("(")) {
            return ParseExprFac();      //表达式因子
        } else if (lexer.peek().equals("x")) {
            return ParseVarFac();       //幂函数因子
        } else if (lexer.peek().equals("sin") || lexer.peek().equals("cos")) {
            return ParseTrigoFac();     //三角函数因子
        } else if (lexer.peek().equals("f")) {
            return ParseRecurFac();     //函数调用因子
        } else if (lexer.peek().equals("g") || lexer.peek().equals("h")) {
            return ParseNormalFac();     //普通函数调用因子
        } else if (lexer.peek().equals("dx")) {
            return ParsederiveFac();
        } else {
            return ParseNumFac();       //常数因子
        }
    }

    private Factor ParseNumFac() {
        StringBuilder num = new StringBuilder();
        if (lexer.peek().equals("-") || lexer.peek().equals("+")) {
            if (lexer.peek().equals("-")) {
                num.append('-');
            }
            lexer.next();
        }
        num.append(lexer.peek());
        lexer.next();
        return new NumFac(num.toString());
    }

    private Factor ParseVarFac() {
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
    }

    private Factor ParseExprFac() {
        lexer.next(); //吃掉 “(”
        Expr expr = parseExpr();
        lexer.next(); //吃掉 “)”
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
    }

    private Factor ParseTrigoFac() {
        String type;
        type = lexer.peek();
        lexer.next();         //来到"("
        lexer.next();         //跳到"("后面的Token
        Expr expr = parseExpr();
        lexer.next();         //跳到")"后面的Token
        BigInteger num = new BigInteger("1");
        if (lexer.peek().equals("^")) {
            lexer.next();
            if (lexer.peek().equals("+")) {
                lexer.next();
            }
            num = new BigInteger(lexer.peek());
            lexer.next();
        }
        return new TrigoFac(type, expr.ToPoly(), num);
    }

    private Factor ParseRecurFac() {
        lexer.next();       //跳过f
        lexer.next();       //跳过{
        NumFac num = (NumFac) ParseNumFac();
        RecurFac recurfac = new RecurFac(Integer.parseInt(num.toString())); //
        lexer.next();       //跳过}
        lexer.next();       //跳过(
        StringBuilder paraX = new StringBuilder();
        StringBuilder paraY = new StringBuilder();
        StringBuilder para = paraX; //不用真的解析factor，直接通过括号找string就行，之后还要重新解析
        int bracket = 1;
        int paraflag = 1;  //用来记录参数的个数 标记是f(x)还是f(x,y)
        while (bracket != 0) {
            if (lexer.peek().equals("(")) {
                bracket += 1;
                para.append(lexer.peek());
            } else if (lexer.peek().equals(")")) {
                bracket -= 1;
                if (bracket != 0) {
                    para.append(lexer.peek());
                }
            } else if (lexer.peek().equals(",") && bracket == 1) {
                recurfac.putpara(1, para.toString());
                para = paraY;
                paraflag = 2;
            } else {
                para.append(lexer.peek());
            }
            lexer.next();
        }
        if (paraflag == 1) {
            recurfac.putpara(1, para.toString());
        } else {
            recurfac.putpara(2, para.toString());
        }
        return recurfac;
    }

    private Factor ParseNormalFac() {
        NormalFac normalfac = new NormalFac(lexer.peek());
        lexer.next();       //跳过g h
        lexer.next();       //跳过(
        StringBuilder paraX = new StringBuilder();
        StringBuilder paraY = new StringBuilder();
        StringBuilder para = paraX;
        int bracket = 1;
        int paraflag = 1;  //用来记录参数的个数 标记是g(x)还是g(x,y)
        while (bracket != 0) {
            if (lexer.peek().equals("(")) {
                bracket += 1;
                para.append(lexer.peek());
            } else if (lexer.peek().equals(")")) {
                bracket -= 1;
                if (bracket != 0) {
                    para.append(lexer.peek());
                }
            } else if (lexer.peek().equals(",") && bracket == 1) {
                normalfac.putpara(1, para.toString());
                para = paraY;
                paraflag = 2;
            } else {
                para.append(lexer.peek());
            }
            lexer.next();
        }
        if (paraflag == 1) {
            normalfac.putpara(1, para.toString());
        } else {
            normalfac.putpara(2, para.toString());
        }
        return normalfac;
    }

    private Factor ParsederiveFac() {
        lexer.next();   // dx
        lexer.next();   // (
        Expr expr = parseExpr();
        lexer.next();   // )
        return new DeriveFac(expr);
    }

}

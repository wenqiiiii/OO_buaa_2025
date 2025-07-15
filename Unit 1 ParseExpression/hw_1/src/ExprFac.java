import java.math.BigInteger;

public class ExprFac implements Factor {
    private Expr expr;
    private BigInteger exp;

    public ExprFac(Expr expr, BigInteger exp) {
        this.expr = expr;
        this.exp = exp;
    }

    public Expr GetExpr() {
        return expr;
    }

    public BigInteger Getexp() {
        return exp;
    }

    public PolyExpr ToPoly() {
        PolyExpr poly = new PolyExpr();
        PolyExpr exprPoly = expr.ToPoly();
        int i = 0;
        while (exp.compareTo(BigInteger.valueOf(i)) > 0) {
            if (poly.IsEmpty()) {
                poly.PolyIn(exprPoly);
            } else {
                poly = poly.MultInPoly(exprPoly);
            }
            i += 1;
        }
        return poly;
    }
}

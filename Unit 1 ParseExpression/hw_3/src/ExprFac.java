import java.math.BigInteger;

public class ExprFac implements Factor {
    private Expr expr;
    private BigInteger exp;

    public ExprFac(Expr expr, BigInteger exp) {
        this.expr = expr;
        this.exp = exp;
    }

    public PolyExpr ToPoly() {
        PolyExpr poly = new PolyExpr();
        PolyExpr exprPoly = expr.ToPoly();
        int i = 0;
        while (exp.compareTo(BigInteger.valueOf(i)) > 0) {
            if (poly.IsEmpty()) {
                poly.putPoly(exprPoly);
            } else {
                poly = poly.MultPoly(exprPoly);
            }
            i += 1;
        }
        return poly;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(expr.toString());
        sb.append(")");
        sb.append("^");
        sb.append(exp.toString());
        return sb.toString();
    }
}

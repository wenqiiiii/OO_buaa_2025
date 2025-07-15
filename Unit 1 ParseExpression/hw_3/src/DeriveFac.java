public class DeriveFac implements Factor {
    private Expr expr;

    public DeriveFac(Expr expr) {
        this.expr = expr;
    }

    public PolyExpr ToPoly() {
        PolyExpr poly = expr.ToPoly();  //要展开待求导表达式，再对其求导
        return poly.derive();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("dx(");
        sb.append(expr.toString());
        sb.append(")");
        return sb.toString();
    }
}
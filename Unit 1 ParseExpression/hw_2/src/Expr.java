import java.util.HashSet;

public class Expr {
    private final HashSet<Term> terms;

    public Expr() {
        this.terms = new HashSet<>();
    }

    public void addTerm(Term term) {
        this.terms.add(term);
    }

    public PolyExpr ToPoly() {
        PolyExpr poly = new PolyExpr();
        for (Term term : terms) {
            PolyExpr termToPoly = term.ToPoly();
            poly.putPoly(termToPoly);   //把各个term相加
        }
        return poly;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Term term : terms) {
            sb.append(term.toString());   //把各个term相加
        }
        return sb.toString();
    }
}

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
            poly.PolyIn(term.ToPoly());   //把各个term相加
        }
        return poly;
    }
}

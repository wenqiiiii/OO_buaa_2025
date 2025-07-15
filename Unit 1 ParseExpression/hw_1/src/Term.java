import java.math.BigInteger;
import java.util.HashSet;

public class Term {
    private HashSet<Factor> factors;
    private char op = '+';

    public Term() {
        this.factors = new HashSet<>();
    }

    public void addFactor(Factor factor) {
        this.factors.add(factor);
    }

    public void ChangeOp() {
        if (this.op == '+') {
            this.op = '-';
        } else {
            this.op = '+';
        }
    }

    public PolyExpr ToPoly() {
        BigInteger coe = new BigInteger("1");
        BigInteger exp = new BigInteger("0");
        PolyExpr poly = new PolyExpr();  //term转化成的多项式
        if (this.op == '-') {
            coe = new BigInteger("-1");
        }
        for (Factor fac : factors) {
            if (fac instanceof NumFac) {
                coe = coe.multiply(((NumFac) fac).Get());
            } else if (fac instanceof VarFac) {
                exp = exp.add(((VarFac) fac).Get());
            } else {    //乘表达式因子
                PolyExpr facPoly = ((ExprFac) fac).ToPoly();
                if (poly.IsEmpty()) {
                    poly.PolyIn(facPoly);
                } else {
                    poly = poly.MultInPoly(facPoly);
                }
            }
        }
        if (poly.IsEmpty()) {
            poly.EasyMonoIn(coe, exp);
        } else {
            poly = poly.MultEasyMono(coe, exp);
        }
        return poly;
    }
}

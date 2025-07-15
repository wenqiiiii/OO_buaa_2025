import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;

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

    public PolyExpr ToPoly() {          //将factors容器中的各因子相乘
        BigInteger coe = new BigInteger("1");
        BigInteger exp = new BigInteger("0");
        PolyExpr poly = new PolyExpr();  //term转化成的多项式
        if (this.op == '-') {
            coe = new BigInteger("-1");
        }
        for (Factor fac : factors) {
            if (fac instanceof NumFac) {        //乘常数因子
                coe = coe.multiply(new BigInteger(((NumFac) fac).toString()));
            } else if (fac instanceof VarFac) {     //变量因子
                exp = exp.add(((VarFac) fac).Get());
            } else if (fac instanceof ExprFac) {        //乘表达式因子
                PolyExpr facPoly = ((ExprFac) fac).ToPoly();
                if (poly.IsEmpty()) {
                    poly.putPoly(facPoly);
                } else {
                    poly = poly.MultPoly(facPoly);
                }
            } else if (fac instanceof TrigoFac) {     //乘三角函数因子
                MonoExpr facmono = new MonoExpr();
                facmono.putTrigoFac((TrigoFac) fac);
                if (poly.IsEmpty()) {
                    poly.putMono(facmono);
                } else {
                    poly = poly.MultMono(facmono);
                }
            } else if (fac instanceof RecurFac) {
                PolyExpr facPoly = ((RecurFac) fac).ToPoly();
                if (poly.IsEmpty()) {
                    poly.putPoly(facPoly);
                } else {
                    poly = poly.MultPoly(facPoly);
                }
            }
        }
        if (poly.IsEmpty()) {
            poly.putPowerFunc(coe, exp);
        } else {
            poly = poly.MultPowerFunc(coe, exp);
        }
        return poly;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(op);
        Iterator iter = factors.iterator();
        while (iter.hasNext()) {
            sb.append(((Factor) iter.next()).toString());
            sb.append("*");
        }
        sb.deleteCharAt(sb.length() - 1); //删掉最后多出来的*
        return sb.toString();
    }
}


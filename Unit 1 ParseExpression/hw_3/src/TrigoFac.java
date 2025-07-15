import java.math.BigInteger;

public class TrigoFac implements Factor {    //type(fac)^exp
    private String type;
    private PolyExpr factor;
    private BigInteger exp;

    public TrigoFac(String t, PolyExpr f, BigInteger e) {
        this.type = t;
        this.factor = f;
        this.exp = e;
    }

    @Override
    public TrigoFac clone() {
        return new TrigoFac(type, factor, exp);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append('(');
        sb.append('(');
        sb.append(factor.print());
        sb.append(')');
        sb.append(')');
        if (exp.toString().equals("1")) {
            return sb.toString();
        } else {
            sb.append('^');
            sb.append(exp.toString());
            return sb.toString();
        }
    }

    public String gettype() {
        return this.type;
    }

    public PolyExpr getfactor() {
        return this.factor;
    }

    public BigInteger getexp() {
        return this.exp;
    }

    public Boolean IsHomo(TrigoFac other) {     //除指数外全都相同
        String t = other.gettype();
        String f = other.getfactor().print();
        if (f.equals(this.factor.print()) && t.equals(this.type)) {
            return true;
        }
        return false;
    }

    public TrigoFac unify(BigInteger p) {       //合并相同三角函数
        return new TrigoFac(type, factor, exp.add(p));
    }

    public PolyExpr derive() {      //(sin(f(x)))^b = ( b*(sin(f(x)))^(b-1) * cos(f(x)) )  *  f'(x)
        PolyExpr derived = new PolyExpr();
        derived.putPoly(factor.derive());    // f'(x)
        MonoExpr mono = new MonoExpr();
        mono.putPowerFunc(exp, new BigInteger("0"));    //b
        if (exp.compareTo(new BigInteger("1")) > 0) {
            mono.putTrigoFac(new TrigoFac(type, factor, exp.subtract(new BigInteger("1"))));
        }       //(sin(f(x)))^(b-1)
        mono.putTrigoFac(new TrigoFac(changetype(), factor, new BigInteger("1"))); // cos(f(x))
        if (type == "cos") {
            mono.negate();
        }
        derived = derived.MultMono(mono);
        return derived;
    }

    private String changetype() {
        if (type.equals("sin")) {
            return "cos";
        } else {
            return "sin";
        }
    }

}

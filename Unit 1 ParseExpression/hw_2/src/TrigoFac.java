import java.math.BigInteger;

public class TrigoFac implements Factor {    //type(fac)^exp
    private String type;
    private String factor;
    private BigInteger exp;

    public TrigoFac(String t, String f, BigInteger e) {
        this.type = t;
        this.factor = f;
        this.exp = e;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append('(');
        sb.append('(');
        sb.append(factor);
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

    public String getfactor() {
        return this.factor;
    }

    public BigInteger getexp() {
        return this.exp;
    }

    public Boolean IsHomo(TrigoFac other) {     //除指数外全都相同
        String t = other.gettype();
        String f = other.getfactor();
        if (f.equals(this.factor) && t.equals(this.type)) {
            return true;
        }
        return false;
    }

    public TrigoFac unify(BigInteger p) {       //合并相同三角函数
        return new TrigoFac(type, factor, exp.add(p));
    }

}

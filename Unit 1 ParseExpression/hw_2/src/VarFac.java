import java.math.BigInteger;

public class VarFac implements Factor {     //x^exp
    private BigInteger exp;

    public VarFac(String e) {
        this.exp = new BigInteger(e);
    }

    public BigInteger Get() {
        return this.exp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("x");
        if (exp.toString().equals("1")) {
            return sb.toString();
        } else {
            sb.append("^");
            sb.append(exp.toString());
            return sb.toString();
        }
    }
}


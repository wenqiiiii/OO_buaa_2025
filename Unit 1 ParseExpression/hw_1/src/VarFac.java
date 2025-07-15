import java.math.BigInteger;

public class VarFac implements Factor {
    private BigInteger exp;

    public VarFac(String e) {
        this.exp = new BigInteger(e);
    }

    public BigInteger Get() {
        return this.exp;
    }
}

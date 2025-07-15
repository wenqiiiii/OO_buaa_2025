import java.math.BigInteger;

public class NumFac implements Factor {
    private final BigInteger num;

    public NumFac(char op, BigInteger n) {
        if (op == '+') {
            this.num = n;
        } else {
            this.num = n.negate();
        }
    }

    public BigInteger Get() {
        return this.num;
    }
}

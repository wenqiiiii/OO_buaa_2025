import java.math.BigInteger;

public class MonoExpr {
    private BigInteger coe;
    private BigInteger exp;

    public MonoExpr(BigInteger coe, BigInteger exp) {
        this.coe = coe;
        this.exp = exp;
    }

    public MonoExpr EasyMult(BigInteger c, BigInteger e) {
        return new MonoExpr(this.coe.multiply(c), this.exp.add(e));
    }

    public BigInteger getcoe() { return this.coe; }

    public BigInteger getexp() {
        return this.exp;
    }

}

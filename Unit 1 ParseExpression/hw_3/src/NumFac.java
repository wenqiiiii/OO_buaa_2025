import java.math.BigInteger;

public class NumFac implements Factor {    //带符号整数
    private final BigInteger num;

    public NumFac(String num) {
        this.num = new BigInteger(num);
    }

    @Override
    public String toString() {
        return num.toString();
    }

}

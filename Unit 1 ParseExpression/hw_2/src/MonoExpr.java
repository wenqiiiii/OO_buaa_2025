import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;

public class MonoExpr {         // cx^e  *  sin(expr1)^e1  *  cos(expr2)^e2  *
    private BigInteger coe = new BigInteger("1");
    private BigInteger exp = new BigInteger("0");
    private HashSet<TrigoFac> trigofacs = new HashSet<>();

    public MonoExpr() {
    }

    public void putPowerFunc(BigInteger c, BigInteger e) {
        coe = coe.multiply(c);
        exp = exp.add(e);
    }

    public void putTrigoFac(TrigoFac trigofac) {
        Boolean had = false;
        TrigoFac same = null;
        for (TrigoFac now : trigofacs) {
            if (now.IsHomo(trigofac)) {
                same = now;
                had = true;
                break;
            }
        }
        if (had) {
            trigofacs.add(same.unify(trigofac.getexp()));
            trigofacs.remove(same);
        } else {
            trigofacs.add(trigofac);
        }
    }

    public MonoExpr MultPowerFunc(BigInteger c, BigInteger e) {
        MonoExpr mono = new MonoExpr();
        mono.putPowerFunc(c.multiply(coe), e.add(exp));
        Iterator tri = trigofacs.iterator();
        while (tri.hasNext()) {
            mono.putTrigoFac((TrigoFac) tri.next());
        }
        return mono;
    }

    public MonoExpr MultMono(MonoExpr a) {
        MonoExpr newone = new MonoExpr();
        newone.putPowerFunc(coe.multiply(a.getcoe()), exp.add(a.getexp()));
        Iterator iter1 = trigofacs.iterator();
        while (iter1.hasNext()) {
            newone.putTrigoFac((TrigoFac) iter1.next());
        }
        Iterator iter2 = a.gettrigofacs().iterator();
        while (iter2.hasNext()) {
            newone.putTrigoFac((TrigoFac) iter2.next());
        }
        return newone;
    }

    public BigInteger getcoe() {
        return this.coe;
    }

    public BigInteger getexp() {
        return this.exp;
    }

    public HashSet<TrigoFac> gettrigofacs() {
        return this.trigofacs;
    }

    public String print() {
        StringBuilder sb = new StringBuilder();
        if (coe.toString().equals("0")) {
            return sb.toString();
        } else if (exp.toString().equals("0")) {
            sb.append("+");
            sb.append(coe.toString());
        } else if (exp.toString().equals("1")) {
            if (coe.toString().equals("1")) {
                sb.append("+x");
            } else {
                sb.append("+");
                sb.append(coe.toString());
                sb.append("*x");
            }
        } else {
            sb.append("+");
            sb.append(coe.toString());
            sb.append("*x^");
            sb.append(exp.toString());
        }
        Iterator iter = trigofacs.iterator();
        while (iter.hasNext()) {
            sb.append("*");
            sb.append(((TrigoFac) iter.next()).toString());
        }
        return sb.toString();
    }

    public Boolean IsUnifiable(MonoExpr other) {   //用于识别同类项（不带三角函数玩）
        if (other.getexp() == exp) {
            if (trigofacs.isEmpty() && other.gettrigofacs().isEmpty()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public MonoExpr unify(MonoExpr one) {    //当两个同类项相加时合并
        MonoExpr unified = new MonoExpr();
        unified.putPowerFunc(coe.add(one.getcoe()), exp);
        return unified;
    }
}

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;

public class PolyExpr {
    private HashSet<MonoExpr> monos;  //key:exp  value:coe

    public PolyExpr() {
        this.monos = new HashSet<>();
    }

    public Boolean IsEmpty() {
        return monos.isEmpty();
    }

    public void putPowerFunc(BigInteger coe, BigInteger exp) {
        MonoExpr mono = new MonoExpr();
        mono.putPowerFunc(coe, exp);
        monos.add(mono);
    }

    public void putMono(MonoExpr mono) {  //把单项式加入
        monos.add(mono);
    }

    public void putPoly(PolyExpr poly) {
        Iterator iter1 = poly.getset().iterator();
        while (iter1.hasNext()) {
            MonoExpr now = (MonoExpr) iter1.next();
            MonoExpr homo = null; //homogeneous 指在monos中找到的同类项
            for (MonoExpr mono : monos) {
                if (mono.IsUnifiable(now)) {
                    homo = mono;
                    break;
                }
            }
            if (homo != null) {
                monos.add(homo.unify(now));
                monos.remove(homo);
            } else {
                monos.add(now);
            }
        }
    }

    public PolyExpr MultPowerFunc(BigInteger coe, BigInteger exp) {
        PolyExpr newone = new PolyExpr();
        if (monos.isEmpty()) {
            newone.putPowerFunc(coe, exp);
        } else {
            Iterator iter2 = monos.iterator();
            while (iter2.hasNext()) {
                newone.putMono(((MonoExpr) iter2.next()).MultPowerFunc(coe, exp));
            }
        }
        return newone;
    }

    public PolyExpr MultMono(MonoExpr mono) {
        Iterator iter3 = monos.iterator();
        PolyExpr newone = new PolyExpr();
        while (iter3.hasNext()) {
            MonoExpr next = (MonoExpr) iter3.next();
            newone.putMono(mono.MultMono(next));
        }
        return newone;
    }

    public PolyExpr MultPoly(PolyExpr poly) {
        Iterator iter4 = monos.iterator();
        PolyExpr newone = new PolyExpr();
        while (iter4.hasNext()) {
            MonoExpr next = (MonoExpr) iter4.next();
            newone.putPoly(poly.MultMono(next));
        }
        return newone;
    }

    public HashSet getset() {
        return this.monos;
    }

    public String print() {
        StringBuilder sb = new StringBuilder();
        Iterator iter4 = monos.iterator();
        while (iter4.hasNext()) {
            sb.append(((MonoExpr) iter4.next()).print());
        }
        if (sb.toString().isEmpty()) {
            sb.append("0");
        }
        return sb.toString();
    }

    public PolyExpr derive() {      //开始求导
        PolyExpr derived = new PolyExpr();
        for (MonoExpr mono : monos) {
            PolyExpr derivedmono = mono.derive();
            derived.putPoly(derivedmono);  //各项求导后相加
        }
        return derived;
    }

}

import java.math.BigInteger;
import java.util.HashMap;
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

    public void EasyMonoIn(BigInteger coe, BigInteger exp) {  //把简单形式的单项式加入
        MonoExpr mono = new MonoExpr(coe, exp);
        monos.add(mono);
    }

    public void MonoIn(MonoExpr mono) {  //把单项式加入
        monos.add(mono);
    }

    public void PolyIn(PolyExpr poly) {   //把多项式加入
        HashSet another = poly.getset();
        Iterator iter1 = another.iterator();
        while (iter1.hasNext()) {
            monos.add((MonoExpr) iter1.next());
        }
    }

    public PolyExpr MultEasyMono(BigInteger coe, BigInteger exp) {
        PolyExpr newone = new PolyExpr();
        if (monos.isEmpty()) {   
            newone.EasyMonoIn(coe, exp);
        } else {
            Iterator iter2 = monos.iterator();
            while (iter2.hasNext()) {
                newone.MonoIn(((MonoExpr) iter2.next()).EasyMult(coe, exp));
            }
        }
        return newone;
    }

    public PolyExpr MultInPoly(PolyExpr poly) {
        Iterator iter3 = monos.iterator();
        PolyExpr newone = new PolyExpr();
        while (iter3.hasNext()) {
            MonoExpr next = (MonoExpr) iter3.next();
            newone.PolyIn(poly.MultEasyMono(next.getcoe(), next.getexp()));
        }
        return newone;
    }

    public HashSet getset() {
        return this.monos;
    }

    private HashMap integrate() {
        HashMap<BigInteger, BigInteger> integrator = new HashMap<>();
        for (MonoExpr mono : monos) {
            BigInteger coe = mono.getcoe();
            BigInteger exp = mono.getexp();
            if (integrator.containsKey(exp)) {  //同类项合并
                coe = coe.add(integrator.get(exp));
                integrator.replace(exp, coe);
            } else {
                integrator.put(exp, coe);
            }
        }
        return integrator;
    }

    public void print() {
        HashMap map = integrate();
        Iterator iter4 = map.keySet().iterator();
        while (iter4.hasNext()) {
            BigInteger key = (BigInteger) iter4.next();
            System.out.printf("+%d*x^%d", map.get(key), key);
        }
    }
}

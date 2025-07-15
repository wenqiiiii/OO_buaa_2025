import java.util.HashMap;

public class RecurFac implements Factor {   //函数调用因子，形如f{num}(para1, para2)
    private int num;
    private HashMap<Integer, String> paras = new HashMap<>();  //存放实参，实现1-2个形参的函数的统一

    public RecurFac(int num) {
        this.num = num;
    }

    public void putpara(Integer num, String para) {
        paras.put(num, para);
    }

    public PolyExpr ToPoly() {
        return RecurUnfolder.getUnfolder().unfold(num, paras);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("f{");
        sb.append(num);
        sb.append("}(");
        sb.append(paras.get(1));
        if (paras.containsKey(2)) {
            sb.append(",");
            sb.append(paras.get(2));
        }
        sb.append(")");
        return sb.toString();
    }
}

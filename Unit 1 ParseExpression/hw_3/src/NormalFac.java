import java.util.HashMap;

public class NormalFac implements Factor {
    private String type;  //type = g or h
    private HashMap<Integer, String> paras = new HashMap<>();

    public NormalFac(String type) {
        this.type = type;
    }

    public void putpara(Integer num, String para) {
        paras.put(num, para);
    }

    public PolyExpr ToPoly() {
        return NormalUnfolder.getUnfolder().unfold(type, paras);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append("(");
        sb.append(paras.get(1));
        if (paras.containsKey(2)) {
            sb.append(",");
            sb.append(paras.get(2));
        }
        sb.append(")");
        return sb.toString();
    }
}

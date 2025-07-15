import java.util.HashMap;

public class NormalUnfolder {
    private String gfuncMold;
    private String hfuncMold;
    private static NormalUnfolder normalUnfolder;

    private NormalUnfolder() {
    }

    public static NormalUnfolder getUnfolder() {     //向外提供该单一实例
        if (normalUnfolder == null) {
            normalUnfolder = new NormalUnfolder();
        }
        return normalUnfolder;
    }

    public void putRule(String r) {
        String rule = new Preprocess(r).process();
        int pos = 0;
        char type;
        type = rule.charAt(pos);
        pos += 2;
        if (rule.charAt(pos) == 'y') {
            if (rule.charAt(pos + 1) == ',') { // 2para
                rule = rule.replace('y', 'z');
                rule = rule.replace('x', 'y');
                rule = rule.replace('z', 'x');
            } else { // 1para
                rule = rule.replace('y', 'x');
            }
        }
        while (rule.charAt(pos) != '=') {
            pos += 1;
        }
        pos += 1;
        StringBuilder sb = new StringBuilder();
        while (pos < rule.length()) {
            sb.append(rule.charAt(pos));
            pos += 1;
        }
        if (type == 'g') {
            gfuncMold = sb.toString();
        } else if (type == 'h') {
            hfuncMold = sb.toString();
        }
    }

    public PolyExpr unfold(String type, HashMap paras) {
        String mold = null;
        if (type.equals("g")) {
            mold = gfuncMold;
        } else if (type.equals("h")) {
            mold = hfuncMold;
        }
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        String x = (String) paras.get(1);
        String y = null;
        if (paras.size() == 2) {
            y = (String) paras.get(2);
        }
        while (pos < mold.length()) {
            if (mold.charAt(pos) == 'x') {
                sb.append("(");
                sb.append(x);
                sb.append(")");
            } else if (mold.charAt(pos) == 'y') {
                sb.append("(");
                sb.append(y);
                sb.append(")");
            } else {
                sb.append(mold.charAt(pos));
            }
            pos += 1;
        }
        String unfolded = sb.toString();
        Parser parse = new Parser(new Lexer(unfolded));
        Expr expr = parse.parseExpr();
        return expr.ToPoly();
    }

}

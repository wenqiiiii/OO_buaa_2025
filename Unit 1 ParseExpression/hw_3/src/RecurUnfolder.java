import java.util.HashMap;
import java.util.HashSet;

public class RecurUnfolder {
    private HashMap<Integer, String> molds = new HashMap<>();
    private String moldfn;
    private int paranum;  //记录了形参个数
    private static RecurUnfolder recurUnfolder;

    private RecurUnfolder() {
    }

    public static RecurUnfolder getUnfolder() {     //向外提供该单一实例
        if (recurUnfolder == null) {
            recurUnfolder = new RecurUnfolder();
        }
        return recurUnfolder;
    }

    public void putRules(String r1, String r2, String r3) {
        HashSet<String> rules = new HashSet<>();
        rules.add(new Preprocess(r1).process());
        rules.add(new Preprocess(r2).process());
        rules.add(new Preprocess(r3).process());
        char n;
        paranum = 1;
        for (String rule : rules) {
            int pos = 0;
            while (rule.charAt(pos) != '{') {
                pos += 1;
            }
            n = rule.charAt(pos + 1);
            while (rule.charAt(pos) != '(') {
                pos += 1;
            }
            pos += 1;
            if (rule.charAt(pos) == 'y') {
                if (rule.charAt(pos + 1) == ',') { //type 2
                    rule = rule.replace('y', 'z');
                    rule = rule.replace('x', 'y');
                    rule = rule.replace('z', 'x');
                } else { //type 1
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
            if (n == '0') {
                molds.put(0, sb.toString());
            } else if (n == '1') {
                molds.put(1, sb.toString());
            } else {
                moldfn = sb.toString();
            }
        }
        generateMolds();
    }

    private void generateMolds() {
        for (int i = 2; i <= 5; i++) {
            generateMold(i);
        }
    }

    private void generateMold(int n) {
        int pos = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 2; i++) {
            while (moldfn.charAt(pos) != 'f') {
                sb.append(moldfn.charAt(pos));
                pos += 1;
            }
            pos += 7;
            int bracket = 1;    //做参数的处理
            StringBuilder x = new StringBuilder();
            StringBuilder y = new StringBuilder();
            StringBuilder p = x;
            while (bracket != 0) {
                if (moldfn.charAt(pos) == '(') {
                    bracket += 1;
                    p.append(moldfn.charAt(pos));
                } else if (moldfn.charAt(pos) == ')') {
                    bracket -= 1;
                    if (bracket != 0) {
                        p.append(moldfn.charAt(pos));
                    }
                } else if (moldfn.charAt(pos) == ',' && bracket == 1) {
                    p = y;
                    paranum = 2;
                } else {
                    p.append(moldfn.charAt(pos));
                }
                pos += 1;
            }
            HashMap<Integer, String> paras = new HashMap<>();
            paras.put(1, x.toString());
            if (paranum == 2) {
                paras.put(2, y.toString());
            }
            sb.append('(');
            sb.append(fn(n - i, paras));
            sb.append(')');
        }
        if (pos < moldfn.length()) {
            int op = 1;
            while ((moldfn.charAt(pos) == '+') || (moldfn.charAt(pos) == '-')) {
                if (moldfn.charAt(pos) == '-') {
                    op = -op;
                }
                pos += 1;
            }
            if (op > 0) {
                sb.append('+');
            } else {
                sb.append('-');
            }
            while (pos < moldfn.length()) {
                sb.append(moldfn.charAt(pos));
                pos += 1;
            }
        }
        molds.put(n, sb.toString());
    }

    private String fn(int n, HashMap paras) {
        String mold = molds.get(n);
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        String x = (String) paras.get(1);
        String y = (String) paras.get(2);
        while (pos < mold.length()) {
            if (paranum == 1) {
                if (mold.charAt(pos) == 'x') {
                    sb.append("(");
                    sb.append(x);
                    sb.append(")");
                } else {
                    sb.append(mold.charAt(pos));
                }
            } else {
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
            }
            pos++;
        }
        return sb.toString();
    }

    public PolyExpr unfold(int n, HashMap paras) {
        String unfolded = fn(n, paras);
        Parser parse = new Parser(new Lexer(unfolded));
        Expr expr = parse.parseExpr();
        return expr.ToPoly();
    }
}
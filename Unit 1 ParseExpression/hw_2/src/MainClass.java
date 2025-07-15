import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String recurFlag = scanner.nextLine();
        if (recurFlag.equals("1")) {
            String rule1 = scanner.nextLine();
            String rule2 = scanner.nextLine();
            String rule3 = scanner.nextLine();
            String input = scanner.nextLine();
            RecurUnfolder unfolder = RecurUnfolder.getUnfolder();
            unfolder.putRules(rule1, rule2, rule3);
            String pre = (new Preprocess(input)).process();     //预处理，去掉了空白符
            Parser parser = new Parser(new Lexer(pre));
            PolyExpr poly = (parser.parseExpr()).ToPoly();
            System.out.printf("%s", poly.print());
        } else {
            String input = scanner.nextLine();
            String pre = (new Preprocess(input)).process();
            Parser parser = new Parser(new Lexer(pre));
            Expr expr = parser.parseExpr();
            PolyExpr poly = (expr).ToPoly();
            System.out.printf("%s", poly.print());
        }
    }
}

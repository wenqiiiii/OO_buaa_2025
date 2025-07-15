import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        Preprocess pre = new Preprocess(input); //预处理，实现了去掉空白符
        Lexer lexer = new Lexer(pre.process());
        Parser parser = new Parser(lexer);
        PolyExpr poly = parser.parseExpr().ToPoly();  //input的解析结果<Expr>.ToPoly
        poly.print();
    }
}

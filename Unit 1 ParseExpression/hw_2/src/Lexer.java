public class Lexer {
    private final String input;
    private int pos = 0;
    private String curToken;

    public Lexer(String input) {
        this.input = input;
        this.next();
    }

    private String getNumber() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            ++pos;
        }
        return sb.toString();
    }

    public void next() {
        if (pos == input.length()) {
            return;
        }  //input是空串或已解析到结尾
        char c = input.charAt(pos);
        if (Character.isDigit(c)) {
            curToken = getNumber();
        } else if (input.charAt(pos) == 's' || input.charAt(pos) == 'c') {        //遇到sin或cos
            if (input.charAt(pos) == 's') {
                curToken = "sin";
            } else if (input.charAt(pos) == 'c') {
                curToken = "cos";
            }
            pos += 3;
        } else if ("()+-*^xf{},".indexOf(c) != -1) {
            pos += 1;
            curToken = String.valueOf(c); //将c转化为字符串类型
        }
    }

    public String peek() {
        return this.curToken; //curToken可能为 + - * ^ ( , ) num x sin cos f { }
    }
}

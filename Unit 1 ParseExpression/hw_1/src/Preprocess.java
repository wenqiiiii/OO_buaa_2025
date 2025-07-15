public class Preprocess {
    private String input;

    public Preprocess(String in) {
        this.input = in;
    }

    public String process() {
        String out = DeleteBlank();
        return out;
    }

    private String DeleteBlank() {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        while (pos < input.length()) {
            if (input.charAt(pos) != ' ' && input.charAt(pos) != '\t') {
                sb.append(input.charAt(pos));
            }
            pos += 1;
        }
        return sb.toString();
    }
}

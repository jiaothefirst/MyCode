package MyRegex;

public class ASTRunner {
    public static void main(String[] args) {

        ASTMatcher astMatcher = ASTMatcher.compile("\\??");
        System.out.println(astMatcher.isMatch("??"));

//        Pattern p = Pattern.compile("\\(123\\)");
//        System.out.println(p.matcher("(123)").find());

    }
}

package MyRegex;

import java.util.Arrays;
import java.util.Objects;

class Terminator {

    /**
     * 数字
     */
    final static String NUMBER = "\\d";
    /**
     * .
     */
    final static String DOT = ".";
    /**
     * 匹配字母，数字或者下划线
     */
    final static String W = "\\W";

    /**
     * 匹配空白符号
     *
     */
    final static String S ="\\S";


    private String type;

    private Character c;
    /**
     * 是否取反
     */
    private boolean isNegative = false;
    private char[] chars;

    Terminator(String type){
        this.type=type;
    }

    Terminator(char c) {
        this.c = c;
    }

    Terminator(boolean isNegative, char[] chars) {
        this.isNegative = isNegative;
        this.chars = chars;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Terminator)) return false;
        Terminator that = (Terminator) o;
        return c == that.c &&
                isNegative == that.isNegative &&
                type.equals(that.type) &&
                Arrays.equals(chars, that.chars);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type, c, isNegative);
        result = 31 * result + Arrays.hashCode(chars);
        return result;
    }

    boolean terminate(char c){

        if(this.c != null){
            return c == this.c;
        }
        if(this.chars != null){
            if(!isNegative){
                for(char ch : chars){
                    if(ch == c){
                        return true;
                    }
                }
                return false;

            } else{
                //取反
                for(char ch : chars){
                    if(ch == c){
                        return false;
                    }
                }
                return true;
            }
        }
        if(type != null){
            switch (type){
                case NUMBER:return c >='0' && c <= '9';
                case DOT:return true;
                case W:return c >= '0' && c <= '9' || c >='a' && c <='z' || c >='A' && c <='Z' || c =='_';
                case S: return c == '\n' || c == '\f' || c == '\r' || c == '\t';
                default:return false;
            }
        }

        return false;
    }


}

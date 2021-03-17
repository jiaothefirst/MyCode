package MyRegex;

import java.util.*;

/**
 * 正则表达式 转 抽象语法树
 */
class RegexToASTree {

    private static final String LEFT_BIG = "{";
    private static final String RIGHT_BIG = "}";
    private static final String LEFT_MID ="[";
    private static final String RIGHT_MID ="]";
    private static final String LEFT_S = "(";
    private static final String RIGHT_S = ")";
    private static final String STAR = "*";
    private static final String ADD = "+";
    private static final String OR = "|";
    private static final String QUESTION = "?";
    private static final String CAT = "cat";
    private static final String TRANS ="\\";
    /**
     * 特殊符号的转义形式
     */
    private static final List<String> trans = Arrays.asList("\\"+LEFT_BIG,"\\"+RIGHT_BIG,
            "\\"+LEFT_MID,"\\"+RIGHT_MID,"\\"+LEFT_S,"\\"+RIGHT_S,"\\"+STAR,"\\"+ADD,"\\"+OR,"\\\\","\\"+QUESTION);

    /**
     *正则表达式转抽象语法树
     */
    static ASTree regexToTree(String regex){
        //获取后缀形式的正则表达式
        List<String> suffix = infixToSuffix(regex);
        //借助栈实现语法树的构建
        Stack<ASTree> nodeStack = new Stack<>();
        for(String str : suffix){
            if(isSimple(str)){
                Terminator terminator = analyseTerminator(str);
                ASTree node = new ASTree(terminator);
                nodeStack.push(node);
            } else {
                if(OR.equals(str)){
                    ASTree term2 = nodeStack.pop();
                    ASTree term1 = nodeStack.pop();
                    ASTree or = new ASTree(ASTree.OR);
                    or.addASTree(term1,term2);
                    nodeStack.push(or);
                } else if(CAT.equals(str)){
                    ASTree term2 = nodeStack.pop();
                    ASTree term1 = nodeStack.pop();
                    ASTree cat = new ASTree(ASTree.CAT);
                    cat.addASTree(term1,term2);
                    nodeStack.push(cat);
                } else{//单操作数运算符 * + {m,n}
                    ASTree term = nodeStack.pop();
                    ASTree singleOpe = analyseNumberType(str);
                    singleOpe.addASTree(term);
                    nodeStack.push(singleOpe);
                }
            }
        }
        //添加起点终点
        ASTree root = new ASTree(ASTree.CAT);
        root.addASTree(nodeStack.pop(),new ASTree(ASTree.END));
        return root;
    }

    /**
     * 中缀形式的字符串转成后缀形式的字符串
     */
    private static List<String> infixToSuffix(String regex){
        List<String> charList = stringToCharList(regex);
        List<String> simples = simplifyRegex(charList);
        List<String> catList = insertCat(simples);
        return toSuffix(catList);
    }

    /**
     *字符串 转  字符列表
     */
    private static List<String> stringToCharList(String regex){
        char[] chars = regex.toCharArray();
        List<String> charList = new ArrayList<>();
        for(char c : chars){
            charList.add(String.valueOf(c));
        }
        return charList;
    }

    /**
     * 简化正则表达式
     *  处理 {m,n},{m},  字符转义，\d，\w, 以及 [abcd] 这种需要多个字符来描述的元素， 将它们合并到一个字符串里面
     *  方便处理
     */
    private static List<String> simplifyRegex(List<String> charList){
        List<String> simpleList = new ArrayList<>();
        int i = 0;
        while(i < charList.size()){
            switch (charList.get(i)){
                case LEFT_BIG:i = analyseBrackets(RIGHT_BIG, charList, simpleList, i);break;
                case LEFT_MID:i = analyseBrackets(RIGHT_MID,charList,simpleList,i);break;
                case TRANS: i = analyseTrans(charList,simpleList,i);break;
                default: simpleList.add(charList.get(i++));break;
            }
        }
        return simpleList;
    }
    /**
     * 插入 cat 连接符号
     * ab 其实是 a cat b 连接符号被省去了
     *  需要加cat的情形共5种
     *
      1.  *) cat (  2. +*{} cat (  3.  +*{} cat  a  4.  a cat a  5. ) cat a

     */
    private static List<String> insertCat(List<String> simples){
        List<String> catList = new ArrayList<>();
        int i=0;
        while(i<simples.size()){
            String str = simples.get(i);
            if(i+1 < simples.size()){
                String next = simples.get(i+1);
                if((priority(str) == 3 && (isSimple(next) || LEFT_S.equals(next))) ||
                        (isSimple(str) && isSimple(next)) ||(RIGHT_S.equals(str) && (LEFT_S.equals(next) ||
                        isSimple(next)))){
                    catList.add(str);
                    catList.add(CAT);
                } else {
                    catList.add(str);
                }
            } else {
                catList.add(str);
            }
            i++;
        }
        return catList;
    }

    /**
     * 转后缀表达式
     */
    private static List<String> toSuffix(List<String> catList){
        int i=0;
        //操作符栈
        Stack<String> opeStack = new Stack<>();

        List<String> suffix = new ArrayList<>();
        while(i<catList.size()){
            String str =catList.get(i);
            if(!isOperate(str)){
                suffix.add(str);
            } else{
                //左括号直接进栈
                if(LEFT_S.equals(str)){
                    opeStack.push(str);
                } else if(RIGHT_S.equals(str)){
                    //右括号出栈
                    while(!opeStack.peek().equals(LEFT_S)){
                        suffix.add(opeStack.pop());
                    }
                    opeStack.pop();
                } else if(!opeStack.isEmpty()){
                    //栈不是空的
                    String pop = opeStack.peek();
                    //操作符 优先级 < 栈顶元素
                    if(!pop.equals(LEFT_S) && priority(str)<= priority(pop)){
                        //出栈， 直到遇到(
                        while(!opeStack.isEmpty() && !opeStack.peek().equals(LEFT_S)){
                            suffix.add(opeStack.pop());
                        }
                        opeStack.push(str);
                    } else {
                        opeStack.push(str);
                    }
                } else{
                    //栈是空的，直接放入
                    opeStack.push(str);
                }
            }
            i++;
        }
        while(!opeStack.isEmpty()){
            suffix.add(opeStack.pop());
        }
        return suffix;
    }

    /**
     * 是否是运算符
     */
    private static boolean isOperate(String str){
        return priority(str) >=0;
    }

    /**
     *是否是字符元素
     */
    private static boolean isSimple(String str){
        return !isOperate(str);
    }

    /**
     * 运算符号优先级
     */
    private static int priority(String str){
        if(LEFT_S.equals(str)){
            return 4;
        }
        if(STAR.equals(str)||ADD.equals(str) || (str.length()>1&&str.contains(LEFT_BIG) && str.contains(RIGHT_BIG)) ||QUESTION.equals(str)){
            return 3;
        }
        if(CAT.equals(str)){
            return 2;
        }
        if(OR.equals(str)){
            return 1;
        }
        if(RIGHT_S.equals(str)){
            return  0;
        }
        return -1;
    }

    /**
     * 解析数量关系
     */
    private static ASTree analyseNumberType(String str){
        if(STAR.equals(str)){
            return new ASTree(ASTree.ZERO_TO_MULTI);
        } else if(ADD.equals(str)){
            return new ASTree(ASTree.ONE_TO_MULTI);
        } else if(QUESTION.equals(str)){
            return new ASTree(ASTree.ZERO_TO_ONE);
        }else{
            //去掉 {,}
            str = str.substring(1,str.length()-1);
            if(!str.contains(",")){
                return new ASTree(ASTree.UN_FIXED,Integer.valueOf(str));
            } else{
                String[] ranges = str.split(",");
                return new ASTree(ASTree.RANGE,Integer.valueOf(ranges[0]),Integer.valueOf(ranges[1]));
            }
        }
    }

    /**
     * 解析括号
     */
    private static int analyseBrackets(String  rightBrackets, List<String> charList, List<String> resultList,int j){
        StringBuilder sb = new StringBuilder();
        while(!rightBrackets.equals(charList.get(j))){
            sb.append(charList.get(j++));
        }
        sb.append(charList.get(j));
        resultList.add(sb.toString());
        return j+1;
    }
    /**
     * 解析转义符号
     */
    private static int analyseTrans(List<String> charList,List<String> resultList,int j){
        resultList.add("\\"+charList.get(j+1));
        return j+2;
    }
    /**
     * 解析终结符
     */
    private static Terminator analyseTerminator(String str){
        if(Terminator.W.equals(str)){
            return new Terminator(Terminator.W);
        } else if(Terminator.DOT.equals(str)){
            return new Terminator(Terminator.DOT);
        } else if(Terminator.NUMBER.equals(str)){
            return new Terminator(Terminator.NUMBER);
        } else if(Terminator.S.equals(str)){
            return new Terminator(Terminator.S);
        } else{
            //普通符号
            if(str.length() == 1){
                return new Terminator(str.charAt(0));
                //转义的符号
            } else if(trans.contains(str)){
                return new Terminator(str.charAt(1));
                //[abc], [a-z] [^a] 类型
            }else{
                str=str.substring(1,str.length()-1);
                int start=0;
                //是否取反
                boolean isNegative =false;
                if(str.charAt(0) == '^'){
                    isNegative = true;
                    start = 1;
                }
                Set<Character> characters = new HashSet<>();
                while(start<str.length()){
                    if(start+2<str.length() && str.charAt(start+1)=='-'){
                        for(char s =str.charAt(start);s<=str.charAt(start+2);s++){
                            characters.add(s);
                        }
                        start+=3;
                    } else {
                        characters.add(str.charAt(start));
                        start++;
                    }
                }
                char[] chars = new char[characters.size()];
                int j=0;
                for(Character c : characters){
                    chars[j++]=c;
                }
                return new Terminator(isNegative,chars);
            }
        }
    }

}

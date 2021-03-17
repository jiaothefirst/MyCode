package MyRegex;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象语法树
 */
class ASTree {

    static final String ZERO_TO_MULTI = "*";
    static final String ONE_TO_MULTI = "+";
    static final String ZERO_TO_ONE = "?";
    static final String UN_FIXED = "un_fixed";
    static final String RANGE = "range";
    static final String CAT = "cat";
    static final String OR =  "or";
    static final String END = "end";
    static final String TERMINATOR = "terminal";

    /**
     * 终结符
     */
    private Terminator terminate;

    /**
     * 操作类型
     */
    private String operateType = ASTree.TERMINATOR;

    /**
     * range或un_fixed操作用到
     */
    private int rangeStart;
    private int rangeEnd;
    private int fixed;

    ASTree(String operateType) {
        this.operateType = operateType;
    }

    ASTree(String operateType, int rangeStart, int rangeEnd) {
        this.operateType = operateType;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }

    Terminator getTerminate() {
        return terminate;
    }

    String getOperateType() {
        return operateType;
    }


    int getRangeStart() {
        return rangeStart;
    }


    int getRangeEnd() {
        return rangeEnd;
    }


    int getFixed() {
        return fixed;
    }


    List<ASTree> getTreeList() {
        return treeList;
    }

    ASTree(String operateType, int fixed) {
        this.operateType = operateType;
        this.fixed = fixed;
    }

    ASTree(Terminator terminate) {
        this.terminate = terminate;
    }

    /**
     * 子节点， 如果是终结符，子节点数量为0
     */
    private List<ASTree> treeList = new ArrayList<>();

    void addASTree (ASTree node){
        treeList.add(node);
    }
    void addASTree(ASTree node1, ASTree node2){
        treeList.add(node1);
        treeList.add(node2);
    }
}

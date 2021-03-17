package MyRegex;

import java.util.*;

public class ASTMatcher {

    private static Map<String,ASTree> treeMap = new HashMap<>();
    private String str;
    private ASTree regexTree;

    private void setStr( String str) {
        this.str = str;
    }

    public static ASTMatcher compile(String regex){
        ASTree tree =treeMap.get(regex);
        if(tree == null){
            tree =RegexToASTree.regexToTree(regex);
            treeMap.put(regex,tree);
        }
        return new ASTMatcher(tree);
    }

    private ASTMatcher(ASTree regexTree) {
        this.regexTree = regexTree;
    }
    public boolean isMatch(String str){
        setStr(str);
        return readASTree(regexTree,0).size() != 0;
    }

    private List<Status> readASTree(ASTree tree, int i){
        switch (tree.getOperateType()){
            case ASTree.CAT:return catNode(tree,i);
            case ASTree.OR:return orNode(tree,i);
            case ASTree.END:return endNode(i);
            case ASTree.ZERO_TO_MULTI:return zero2MultiNode(tree,i);
            case ASTree.ONE_TO_MULTI:return one2MultiNode(tree,i);
            case ASTree.RANGE:return rangeNode(tree,i);
            case ASTree.UN_FIXED:return fixedNode(tree,i);
            case ASTree.ZERO_TO_ONE:return zero2One(tree,i);
            default:return terminatorNode(tree,i);
        }
    }
    private List<Status> catNode(ASTree tree, int i){
        List<ASTree> nodes = tree.getTreeList();
        Set<Status> statuses = new HashSet<>();
        statuses.add(new Status(i));
        for(ASTree node : nodes){
            Set<Status> tempStatus = new HashSet<>();
            for(Status status : statuses){
                List<Status> list = readASTree(node,status.getI());
                tempStatus.addAll(list);
            }
            if(tempStatus.size() == 0){
                return new ArrayList<>();
            }
            statuses = tempStatus;
        }
        return new ArrayList<>(statuses);
    }
    private List<Status> orNode(ASTree tree, int i){
        List<ASTree> nodes = tree.getTreeList();
        List<Status> result = new ArrayList<>();
        for(ASTree node : nodes){
            result.addAll(readASTree(node,i));
        }
        return result;
    }
    private List<Status> zero2MultiNode(ASTree tree,int i){
        return search(0,0,Integer.MAX_VALUE,tree.getTreeList().get(0),i);
    }
    private List<Status> one2MultiNode(ASTree tree, int i){
        return search(1,0,Integer.MAX_VALUE,tree.getTreeList().get(0),i);
    }
    private List<Status> rangeNode(ASTree tree, int i){
        return search(tree.getRangeStart(),tree.getRangeStart(),
                tree.getRangeEnd(),tree.getTreeList().get(0),i);
    }
    private List<Status> fixedNode(ASTree tree, int i){
        return search(tree.getFixed(),0,0,tree.getTreeList().get(0),i);
    }
    private List<Status> zero2One(ASTree tree,int i){
        return search(0,0,1,tree.getTreeList().get(0),i);
    }

    /**
     *产生的新状态的数量
     */
    private int newStatusSize(Set<Status> usedStatus,Set<Status> cur){
        int size = 0;
        for(Status s : cur){
            if(!usedStatus.contains(s)){
                size++;
            }
        }
        return size;
    }

    private List<Status> terminatorNode(ASTree tree, int i){
        List<Status> statuses = new ArrayList<>();
        if(i < str.length() && tree.getTerminate().terminate(str.charAt(i))){
            statuses.add(new Status(i+1));
        }
        return statuses;
    }
    private List<Status> endNode(int i){
        List<Status> statuses = new ArrayList<>();
        if(i == str.length()){
            statuses.add(new Status(i));
        }
        return statuses;
    }

    private List<Status> search(int fixed, int rangeStart,int rangeEnd,ASTree childNode,int i){
        Set<Status> lastStatus = new HashSet<>();
        Status init = new Status(i);
        Set<Status> used = new HashSet<>();
        lastStatus.add(init);
        for(int j=0;j<fixed;j++){
            Set<Status> curStatus = new HashSet<>();
            for(Status s : lastStatus){
                List<Status> list = readASTree(childNode,s.getI());
                curStatus.addAll(list);
            }
            //没有产生状态，直接返回
            if(curStatus.size() == 0){
                return new ArrayList<>();
            }
            lastStatus = curStatus;
        }
        Set<Status> statuses = new HashSet<>(lastStatus);
        //重置 used
        used.clear();
        int j= rangeStart;
        while(j < rangeEnd){
            Set<Status> curStatus = new HashSet<>();
            for(Status s : lastStatus){
                if(!used.contains(s)){
                    used.add(s);
                    List<Status> list = readASTree(childNode,s.getI());
                    curStatus.addAll(list);
                }
            }
            //没有产生新的状态直接结束
            if(newStatusSize(used,curStatus) == 0){
                break;
            }
            lastStatus = curStatus;
            statuses.addAll(lastStatus);
            j++;
        }
        return new ArrayList<>(statuses);
    }
}
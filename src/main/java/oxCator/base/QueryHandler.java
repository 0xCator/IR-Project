package oxCator.base;

import java.util.*;





enum NodeType {
    TERM, NOT, OR, AND, COUNT
}

class ASTnode{
    NodeType nodeType; 
    ASTnode left; 
    ASTnode right; 
    String value;

    public static  ASTnode mkastnode(NodeType nodeType, ASTnode left, ASTnode right){
        ASTnode node = new ASTnode();
        node.nodeType = nodeType;
        node.left = left;
        node.right = right;
        return node;
    }
    public static ASTnode mkastleaf(NodeType nodeType, String value){
        ASTnode node = new ASTnode();
        node.nodeType = nodeType;
        node.value = value;
        node.left = null;
        node.right = null;
        return node;
    }
    public static ASTnode mkastunary(NodeType nodeType, ASTnode right){
        ASTnode node = new ASTnode();
        node.nodeType = nodeType;
        node.right = right;
        node.left = null;
        return node;
    }
}


public class QueryHandler {
    private HashMap<String, PositionalIndex> positionalIndex;
    private Queue<String> termQueue;
    private Stack<ArrayList<Integer>> matrixStack = new Stack<>();
    private HashSet<Integer> documentSet;
    private ArrayList<String> tokens = new ArrayList<>();
    private String token;
    private int idx;
    private String query;
    QueryHandler(HashMap<String, PositionalIndex> positionalIndex, int docNumber) {
        this.positionalIndex = positionalIndex;
        documentSet = new HashSet<>();
        for (int i = 0; i < docNumber; i++)
            documentSet.add(i);
    }

    private void nextToken() {
        if (idx < tokens.size())
            token = tokens.get(idx++);
        else
            token = null;
    }
    public void processQuery(String query){
        this.query = query;
        tokens.clear(); 
        idx = 0;
        String[] terms = query.split(" ");
        int j = 0;
        for(int i = 0; i < terms.length; i++){
            if(terms[i].equals("AND") || terms[i].equals("OR") || terms[i].equals("NOT")){
                String tmp = "";
                for(int k = j; k < i; k++){
                    tmp += terms[k] + " ";
                }
                if(!tmp.equals("")){
                    tmp = tmp.trim();
                    tokens.add(tmp);
                }
                j = i + 1;
                tokens.add(terms[i]);
            }
            if(i == terms.length - 1){
                String tmp = "";
                for(int k = j; k <= i; k++){
                    tmp += terms[k] + " ";
                }
                if(!tmp.equals("")){
                    tmp = tmp.trim();
                    tokens.add(tmp);
                }
            }
        }

        nextToken();
    }
    public ArrayList<String> getTokens(){
        String[] queryTokens = query.split(" ");
        ArrayList<String> tokens = new ArrayList<>();
        for(int i =0; i< queryTokens.length; i++){
            if(queryTokens[i].equals("NOT")){
                i++;
                continue;

            }else if(queryTokens[i].equals("OR") || queryTokens[i].equals("AND"))
                continue;
            tokens.add(queryTokens[i]);
        }
        System.out.println(tokens);
        return tokens;
    }
    
    private ASTnode primary(){
        ASTnode node = null; 
        if(token.equals("NOT")){
            nextToken();
            node = ASTnode.mkastunary(NodeType.NOT, primary());
        } else if(token.equals("AND") || token.equals("OR")){
            throw new RuntimeException("Syntax error");
        }else{
            node = ASTnode.mkastleaf(NodeType.TERM, token);
            nextToken();
        }

        return node;

    }

    private int precedence(NodeType nodeType){
        switch(nodeType){
            case NOT:
                return 3;
            case AND:
                return 2;
            case OR:
                return 1;
            default:
                return 0;
        }
    }
    private NodeType nodetype(String token){
        switch(token){
            case "NOT":
                return NodeType.NOT;
            case "AND":
                return NodeType.AND;
            case "OR":
                return NodeType.OR;
            default:
                return NodeType.TERM;
        }
    }

    private ASTnode expr(int ptp){
        ASTnode left, right;
        NodeType nodeType;
        left = primary();

        if(token == null){
            return left;
        }

        nodeType = nodetype(token);
        while(precedence(nodeType) > ptp){
            nextToken(); 
            right = expr(precedence(nodeType));
            left = ASTnode.mkastnode(nodeType, left, right);
            if(token == null){
                return left;
            }
            nodeType = nodetype(token);
        }
        return left;

    }
    public void printAST(ASTnode node){
        if(node.nodeType == NodeType.TERM){
            System.out.print(node.value);
        }
        else{
            System.out.print("(");
            printAST(node.left);
            switch(node.nodeType){
                case NOT:
                    System.out.print(" NOT ");
                    break;
                case AND:
                    System.out.print(" AND ");
                    break;
                case OR:
                    System.out.print(" OR ");
                    break;
                default:
                    break;
            }
            printAST(node.right);
            System.out.print(")");
        }
    }

    public ArrayList<Integer> makeQuery(String Query){

        processQuery(Query);
        ASTnode node = expr(0);
        genResult(node);
        if(!matrixStack.isEmpty()){
            return matrixStack.pop();
        }
        return null;
    }

    private void genResult(ASTnode node){
        if(node == null){
            return;
        }
        genResult(node.left);
        genResult(node.right);
        switch(node.nodeType){
            case TERM:
                matrixStack.push(phraseQuery(node.value));
                break;
            case NOT:
                notOperation();
                break;
            case AND:
                andOperation();
                break;
            case OR:
                orOperation();
                break;
            default:
                break;
        }
    }

    private ArrayList<Integer> phraseQuery(String query) {
        termQueue = new LinkedList<>();
        PositionalIndex result = null;
        Tokenizer tokenizer = new Tokenizer();
        ArrayList<String> termList = tokenizer.tokenize(query);

        for (String term : termList) {
            termQueue.offer(term);
        }

        if (termQueue.size() == 1) {
            //A single word returns its posting list (documents only) as an array list)
            // if the word is not in the index, return an empty array list
            if (!positionalIndex.containsKey(termQueue.peek()))
                return new ArrayList<>();
            return new ArrayList<>(positionalIndex.get(termQueue.poll()).getPostingList().keySet());
        }

        while (!termQueue.isEmpty()) {
            //Merges a sequence of words
            if (result == null) {
                result = positionalIndex.get(termQueue.poll());
            }
            if(!positionalIndex.containsKey(termQueue.peek())){
                return new ArrayList<>();
            }
            PositionalIndex term2 = positionalIndex.get(termQueue.poll());

            result = mergeFiles(result, term2);
        }

        return new ArrayList<>(result.getPostingList().keySet());
    }

    //Used to merge files on an outer level
    private PositionalIndex mergeFiles(PositionalIndex term1, PositionalIndex term2) {
        PositionalIndex result = new PositionalIndex();
        HashMap<Integer, ArrayList<Integer>> list1, list2;
        list1 = term1.getPostingList();
        list2 = term2.getPostingList();
        ArrayList<Integer> fileIDs1, fileIDs2;
        fileIDs1 = new ArrayList<>(list1.keySet());
        fileIDs2 = new ArrayList<>(list2.keySet());

        int p1 = 0, p2 = 0;

        while (p1 != fileIDs1.size() && p2 != fileIDs2.size()) {
            int i1 = fileIDs1.get(p1), i2 = fileIDs2.get(p2);
            if (i1 == i2) {
                //Inner merging
                ArrayList<Integer> mergeResult = mergePositions(list1.get(i1), list2.get(i2));
                //Avoid empty arrayLists (same files, no positions)
                if (!mergeResult.isEmpty())
                    result.addPosition(i1, mergeResult);
                p1++; p2++;
            } else if (i1 > i2) {
                p2++;
            } else {
                p1++;
            }
        }
        return result;
    }

    //Merges positions, in consecutive order
    private ArrayList<Integer> mergePositions(ArrayList<Integer> posList1, ArrayList<Integer> posList2) {
        ArrayList<Integer> result = new ArrayList<>();
        int p1 = 0, p2 = 0;

        while (p1 != posList1.size() && p2 != posList2.size()) {
            int i1 = posList1.get(p1), i2 = posList2.get(p2);
            if (i1 == i2-1) {
                result.add(i2);
                p1++; p2++;
            } else if (i1 > i2) {
                p2++;
            } else {
                p1++;
            }
        }

        return result;
    }
    private void notOperation() {
        ArrayList<Integer> result = new ArrayList<>(documentSet);
        ArrayList<Integer> termMatrix = matrixStack.pop();

        for (int document : termMatrix)
            result.remove(result.indexOf(document));

        matrixStack.push(result);
    }
    private void orOperation() {
        HashSet<Integer> result = new HashSet<>();

        ArrayList<Integer> mat1 = new ArrayList<>(matrixStack.pop());
        ArrayList<Integer> mat2 = new ArrayList<>(matrixStack.pop());

        //Adding all values of each term, hashsets will handle the repetition
        for (int i : mat1)
            result.add(i);

        for (int i : mat2)
            result.add(i);

        matrixStack.push(new ArrayList<>(result));
    }
    private void andOperation() {
        int p1 = 0, p2 = 0;

        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<Integer> mat1 = new ArrayList<>(matrixStack.pop());
        ArrayList<Integer> mat2 = new ArrayList<>(matrixStack.pop());

        //Same implementation as the lecture
        while (p1 != mat1.size() && p2 != mat2.size()) {
            int i1 = mat1.get(p1), i2 = mat2.get(p2);
            if (i1 == i2) {
                result.add(mat1.get(p1));
                p1++; p2++;
            } else if (i1 > i2) {
                p2++;
            } else {
                p1++;
            }
        }

        matrixStack.push(result);
    }
}

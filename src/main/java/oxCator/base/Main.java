package oxCator.base;

import java.util.*;

public class Main {
    public static String folderPath = "/files";
    public static HashMap<String, PositionalIndex> positionalIndex;
    public static HashMap<Integer, String> fileList;
    public static Preprocessor preProc;
    public static QueryHandler handler;

    public static void main(String[] args) {
        preProc = new Preprocessor(folderPath);
        fileList = preProc.getFilenameList();
        positionalIndex = preProc.getPositionalIndex();

        handler = new QueryHandler(positionalIndex, fileList.size());
        Scanner input = new Scanner(System.in);

        System.out.println("Welcome to the best IR system, part two!\nEnter Query:");
        String queryLine = input.nextLine();

        while (!queryLine.equalsIgnoreCase("stop")) {
            ArrayList<Integer> result = handler.makeQuery(queryLine);
            System.out.println(handler.getTokens());
            printResults(result);
            System.out.println("Enter 'stop' to end, or another query: ");
            queryLine = input.nextLine();
        }
    }

    public static void printResults(ArrayList<Integer> resultList) {
        if (resultList.size() != 0) {
            System.out.println("Files found:");
            for (int docID : resultList) {
                System.out.println(fileList.get(docID));
            }
        } else {
            System.out.println("Empty query");
        }
    }
}

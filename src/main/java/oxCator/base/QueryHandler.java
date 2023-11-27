package oxCator.base;

import java.util.*;

public class QueryHandler {
    private static HashMap<String, PositionalIndex> positionalIndex;
    private Queue<String> termQueue;
    private HashSet<Integer> documentSet;
    private PorterStemmer stemmer = new PorterStemmer();

    QueryHandler(HashMap<String, PositionalIndex> positionalIndex, int docNumber) {
        this.positionalIndex = positionalIndex;
        documentSet = new HashSet<>();
        for (int i = 0; i < docNumber; i++)
            documentSet.add(i);
    }

    public ArrayList<Integer> makeQuery(String query) {
        termQueue = new LinkedList<>();
        PositionalIndex result = null;
        String[] termList = query.split(" ");

        for (String term : termList) {
            termQueue.offer(stemmer.stem(term.trim().toLowerCase()));
        }

        if (termQueue.size() == 1) {
            //A single word returns its posting list (documents only) as an array list)
            return new ArrayList<>(positionalIndex.get(termQueue.poll()).getPostingList().keySet());
        }

        while (!termQueue.isEmpty()) {
            //Merges a sequence of words
            if (result == null) {
                result = positionalIndex.get(termQueue.poll());
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
}

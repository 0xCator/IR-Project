package oxCator.base;

import java.util.*;

public class PositionalIndex {
    private HashMap<Integer, ArrayList<Integer>> postingList;
    private int documentFrequency;

    PositionalIndex() {
        postingList = new HashMap<>();
        documentFrequency = 0;
    }

    //Adds a *single* position per document
    public void addPosition(int docID, int position) {
        if (postingList.get(docID) == null) {
            ArrayList<Integer> docPostingList = new ArrayList<>();
            docPostingList.add(position);
            postingList.put(docID, docPostingList);
            documentFrequency++;
        } else {
            postingList.get(docID).add(position);
        }
    }

    //Add an entire list for a *single* position
    public void addPosition(int docID, ArrayList<Integer> positionList) {
        if (postingList.get(docID) == null) {
            postingList.put(docID, positionList);
            documentFrequency++;
        }
    }

    public HashMap<Integer, ArrayList<Integer>> getPostingList() {
        return postingList;
    }

    public int getDocumentFrequency() {
        return documentFrequency;
    }
}

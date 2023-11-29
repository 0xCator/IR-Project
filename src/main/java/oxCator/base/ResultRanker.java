package oxCator.base;

import javax.print.Doc;
import java.util.*;

public class ResultRanker {
    private HashMap<String, ArrayList<Double>> normalizedTF_IDF;
    private HashMap<String, Double> idf;

    ResultRanker(HashMap<String, ArrayList<Double>> tfIDF, HashMap<String, Double> idf) {
        this.normalizedTF_IDF = tfIDF;
        this.idf = idf;
    }

    public ArrayList<Integer> rank(ArrayList<Integer> files, ArrayList<String> query) {
        ArrayList<Double> scores = calculateSimilarity(files, query);
        return orderList(files, scores);
    }

    private ArrayList<Double> calculateSimilarity(ArrayList<Integer> files, ArrayList<String> query) {
        ArrayList<Double> result = new ArrayList<>();
        //Step 1: Create query vector
        DocumentVector queryVector = new DocumentVector(query);
        for (String term : query)
            queryVector.setDimension(term, idf.get(term));
        ArrayList<Double> normalizedQueryVector = queryVector.getUnitVector();

        //Step 2: Create document(s) vector
        ArrayList<DocumentVector> documentVectors = new ArrayList<>();
        for (int file : files) {
            DocumentVector vector = new DocumentVector(query);
            for (String term : query)
                vector.setDimension(term, normalizedTF_IDF.get(term).get(file));
            documentVectors.add(vector);
        }

        //Step 3: Dot Product
        for (DocumentVector vector : documentVectors)
            result.add(dotProduct(normalizedQueryVector, vector.getVector()));

        return result;
    }

    private ArrayList<Integer> orderList(ArrayList<Integer> files, ArrayList<Double> scores) {

        return new ArrayList<>();
    }

    private double dotProduct(ArrayList<Double> normVec1, ArrayList<Double> normVec2) {
        double result = 0.0;
        for (int i = 0; i < normVec1.size(); i++)
            result += normVec1.get(i) * normVec2.get(i);
        return result;
    }
}

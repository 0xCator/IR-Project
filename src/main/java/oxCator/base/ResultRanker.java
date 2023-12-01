package oxCator.base;

import java.util.*;

public class ResultRanker {
    private HashMap<String, ArrayList<Double>> normalizedTF_IDF;
    private HashMap<String, Double> idf;
    private PorterStemmer stemmer = new PorterStemmer();

    ResultRanker(HashMap<String, ArrayList<Double>> tfIDF, HashMap<String, Double> idf) {
        this.normalizedTF_IDF = tfIDF;
        this.idf = idf;
    }

    public ArrayList<Integer> rank(ArrayList<Integer> files, ArrayList<String> query) {
        //Stem the query tokens
        for (int i = 0; i < query.size(); i++)
            query.set(i, stemmer.stem(query.get(i)));
        ArrayList<Double> scores = calculateSimilarity(files, query);
        return orderList(files, scores);
    }

    private ArrayList<Double> calculateSimilarity(ArrayList<Integer> files, ArrayList<String> query) {
        ArrayList<Double> result = new ArrayList<>();
        ArrayList<String> uniqueQuery = makeUnique(query);

        //Step 1: Create query vector
        DocumentVector queryVector = new DocumentVector(uniqueQuery);
        for (String term : uniqueQuery) {
            //Count the frequency of the term IN THE QUERY
            double termTF = 1 + Math.log10(Collections.frequency(query, term));
            queryVector.setDimension(term, termTF * idf.get(term));
        }
        ArrayList<Double> normalizedQueryVector = queryVector.getUnitVector();

        //Step 2: Create document(s) vector
        ArrayList<DocumentVector> documentVectors = new ArrayList<>();
        for (int file : files) {
            DocumentVector vector = new DocumentVector(uniqueQuery);
            for (String term : uniqueQuery) {
                vector.setDimension(term, normalizedTF_IDF.get(term).get(file));
            }
            documentVectors.add(vector);
        }

        //Step 3: Dot Product
        for (DocumentVector vector : documentVectors)
            result.add(dotProduct(normalizedQueryVector, vector.getVector()));

        return result;
    }

    private ArrayList<Integer> orderList(ArrayList<Integer> files, ArrayList<Double> scores) {
        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<Integer> filesCopy = (ArrayList<Integer>) files.clone();
        ArrayList<Double> scoreCopy = (ArrayList<Double>) scores.clone();
        while (result.size() != files.size()) {
            double max = 0.0;
            for (double score : scoreCopy)
                max = Math.max(max, score);
            int index = scoreCopy.indexOf(max);
            result.add(filesCopy.get(index));
            filesCopy.remove(index);
            scoreCopy.remove(index);
        }
        return result;
    }

    private double dotProduct(ArrayList<Double> normVec1, ArrayList<Double> normVec2) {
        double result = 0.0;
        for (int i = 0; i < normVec1.size(); i++)
            result += normVec1.get(i) * normVec2.get(i);
        return result;
    }

    private ArrayList<String> makeUnique(ArrayList<String> list) {
        //Creates a hashset
        HashSet<String> set = new HashSet<>();
        set.addAll(list);
        ArrayList<String> uniqueList = new ArrayList<>(set);
        return uniqueList;
    }
}

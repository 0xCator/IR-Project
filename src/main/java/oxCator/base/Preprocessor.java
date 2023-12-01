package oxCator.base;

import java.io.*;
import java.util.*;

public class Preprocessor {
    private String folderPath;
    private HashMap<Integer, String> filenameList = new HashMap<>();
    private HashMap<String, PositionalIndex> positionalIndex = new HashMap<>();
    private HashMap<String, ArrayList<Integer>> termFrequency = new HashMap<>();
    private HashMap<String, ArrayList<Double>> weightedTF, tfIDF, normalizedTF_IDF;
    private HashMap<String, Double> df, idf;
    private HashMap<Integer, Double> documentLength = new HashMap<>();
    private Tokenizer tokenizer = new Tokenizer();

    Preprocessor(String filePath) {
        folderPath = new File("").getAbsolutePath() + filePath;
        readFiles();
        generateMatrices();
    }

    private void readFiles() {
        File folder = new File(folderPath);
        File[] fileList = folder.listFiles();
        int fileCounter = 0;
        for (File file : fileList) {
            if (file.isFile()) {

                //Identify each file with an ID number
                filenameList.put(fileCounter, file.getName());
                //HashMap<String, Integer> wordCounts = new HashMap<>(); // Inner map for word counts

                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));

                    String line = reader.readLine();

                    int wordCounter = 0;

                    while (line != null) {
                        ArrayList<String> words = tokenizer.tokenize(line);
                        for (String word : words) {

                            //Term frequency creation
                            if (termFrequency.get(word) == null) {
                                //If the word isn't captured, generate a matrix (zeroes, document size)
                                ArrayList<Integer> termVector = new ArrayList<>();
                                for (int i = 0; i < fileList.length; i++)
                                    termVector.add(0);
                                termVector.set(fileCounter,termVector.get(fileCounter)+1);
                                termFrequency.put(word, termVector);
                            } else {
                                //If the word is already caught, increment
                                ArrayList<Integer> termVector = termFrequency.get(word);
                                termVector.set(fileCounter, termVector.get(fileCounter)+1);
                            }

                            //Positional indexing
                            if (positionalIndex.get(word) == null) {
                                //If the word isn't captured before, add it to the matrix
                                PositionalIndex pos = new PositionalIndex();
                                pos.addPosition(fileCounter, wordCounter);
                                positionalIndex.put(word, pos);
                                //discTerm.add(word);
                            } else {
                                //Otherwise, add the current file ID to this word's posting list
                                positionalIndex.get(word).addPosition(fileCounter, wordCounter);
                            }
                            wordCounter++;
                        }
                        line = reader.readLine();
                    }
                    //documentWordCounts.put(fileCounter, wordCounts); // Store word counts for the current document
                    fileCounter++;
                    reader.close();
                } catch (Exception e) {
                    System.err.println("Exception in Preprocessor class");
                    System.err.println(e.getMessage());
                }
            }
        }

    }

    private void generateMatrices() {
        //Generate weighted TF
        weightedTF = new HashMap<>();
        for (String key : termFrequency.keySet()) {
            ArrayList<Integer> regularTFVector = termFrequency.get(key);
            ArrayList<Double> vector = new ArrayList<>();

            for (int value : regularTFVector) {
                if (value != 0)
                    vector.add(1 + Math.log10(value));
                else vector.add(0.0);
            }

            weightedTF.put(key, vector);
        }

        //Generate DF
        df = new HashMap<>();
        for (String key : positionalIndex.keySet())
            df.put(key, (double) positionalIndex.get(key).getDocumentFrequency());

        //Generate IDF
        idf = (HashMap<String, Double>) df.clone();
        for (String key : idf.keySet())
            idf.replace(key, Math.log10(filenameList.size() / idf.get(key)));

        //Generate TF-IDF
        tfIDF = (HashMap<String, ArrayList<Double>>) weightedTF.clone();
        for (String key : tfIDF.keySet()) {
            ArrayList<Double> vector = (ArrayList<Double>) tfIDF.get(key).clone();
            for (int i = 0; i < vector.size(); i++)
                vector.set(i, vector.get(i) * idf.get(key));
            tfIDF.replace(key, vector);
        }

        //Generate length
        for (int i = 0; i < filenameList.size(); i++) {
            double length = 0.0;
            for (ArrayList<Double> weight : tfIDF.values())
                length += Math.pow(weight.get(i), 2);
            length = Math.sqrt(length);
            documentLength.put(i, length);
        }

        //Generate normalized TF-IDF
        normalizedTF_IDF = (HashMap<String, ArrayList<Double>>) tfIDF.clone();
        for (String key : normalizedTF_IDF.keySet()) {
            ArrayList<Double> vector = (ArrayList<Double>) normalizedTF_IDF.get(key).clone();
            for (int i = 0; i < vector.size(); i++)
                vector.set(i, vector.get(i) / documentLength.get(i));
            normalizedTF_IDF.replace(key, vector);
        }
    }

    public HashMap<Integer, String> getFilenameList() {
        return filenameList;
    }

    public HashMap<String, PositionalIndex> getPositionalIndex() {
        return positionalIndex;
    }
    public HashMap<String, ArrayList<Integer>> getTermFrequency() {
        return termFrequency;
    }
    public HashMap<String, Double> getDf() {
        return df;
    }
    public HashMap<String, ArrayList<Double>> getWeightedTF() {
        return weightedTF;
    }
    public HashMap<String, ArrayList<Double>> getTfIDF() {
        return tfIDF;
    }
    public HashMap<String, ArrayList<Double>> getNormalizedTF_IDF() {
        return normalizedTF_IDF;
    }
    public HashMap<String, Double> getIdf() {
        return idf;
    }
    public HashMap<Integer, Double> getDocumentLength() {
        return documentLength;
    }

    public static void main(String[] args) {
        Preprocessor preprocessor = new Preprocessor("/files");
    }
}

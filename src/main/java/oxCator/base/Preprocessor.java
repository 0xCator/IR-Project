package oxCator.base;

import java.io.*;
import java.util.*;

public class Preprocessor {
    private String folderPath;
    private HashMap<Integer, String> filenameList = new HashMap<>();
    private HashMap<String, PositionalIndex> positionalIndex = new HashMap<>();
    // Outer map to store word counts for each document
    private HashMap<Integer, HashMap<String, Integer>> documentWordCounts = new HashMap<>();
    private HashSet<String> discTerm = new HashSet<>();
    private HashMap<String, ArrayList<Integer>> termFrequency = new HashMap<>();
    private Tokenizer tokenizer = new Tokenizer();

    Preprocessor(String filePath) {
        folderPath = new File("").getAbsolutePath() + filePath;
        readFiles();
    }

    private void readFiles() {
        File folder = new File(folderPath);
        File[] fileList = folder.listFiles();
        int fileCounter = 0;
        for (File file : fileList) {
            if (file.isFile()) {

                //Identify each file with an ID number
                filenameList.put(fileCounter, file.getName());
                HashMap<String, Integer> wordCounts = new HashMap<>(); // Inner map for word counts

                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()));

                    String line = reader.readLine();

                    int wordCounter = 0;

                    while (line != null) {
                        ArrayList<String> words = tokenizer.tokenize(line);
                        for (String word : words) {

                            // Update word counts for the current document
                            wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);

                            //Positional indexing
                            if (positionalIndex.get(word) == null) {
                                //If the word isn't captured before, add it to the matrix
                                PositionalIndex pos = new PositionalIndex();
                                pos.addPosition(fileCounter, wordCounter);
                                positionalIndex.put(word, pos);
                                discTerm.add(word);
                            } else {
                                //Otherwise, add the current file ID to this word's posting list
                                positionalIndex.get(word).addPosition(fileCounter, wordCounter);
                            }
                            wordCounter++;
                        }
                        line = reader.readLine();
                    }
                    documentWordCounts.put(fileCounter, wordCounts); // Store word counts for the current document
                    fileCounter++;
                    reader.close();
                } catch (Exception e) {
                    System.err.println("Exception in Preprocessor class");
                    System.err.println(e.getMessage());
                }
            }
        }
        for (String word : discTerm) {

            // Initialize the ArrayList for the current word
            ArrayList<Integer> termFrequencyList = new ArrayList<>();

            // Iterate over each document
            for (Map.Entry<Integer, HashMap<String, Integer>> entry : documentWordCounts.entrySet()) {
                HashMap<String, Integer> wordCountMap = entry.getValue();

                // Check if the term exists in the current document
                int frequency = wordCountMap.getOrDefault(word, 0);

                // Add the term frequency to the list
                termFrequencyList.add(frequency);
            }

            // Put the word and its term frequency list in the new HashMap
            termFrequency.put(word, termFrequencyList);
        }
    }

    public HashMap<Integer, String> getFilenameList() {
        return filenameList;
    }

    public HashMap<String, PositionalIndex> getPositionalIndex() { return positionalIndex; }
    public HashMap<String, ArrayList<Integer>> getTermFrequency(){return termFrequency;}
}

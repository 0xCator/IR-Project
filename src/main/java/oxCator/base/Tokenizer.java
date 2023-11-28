package oxCator.base;

import java.util.*;

public class Tokenizer {
    private PorterStemmer stemmer = new PorterStemmer();
    public ArrayList<String> tokenize(String line) {
        ArrayList<String> result = new ArrayList<>();
        //Replacing any line-breaks with a space
        line = line.replaceAll("[\n]"," ");
        for (String word : line.split(" ")) {
            word = word.trim();
            //Removing all periods and commas
            word = word.replaceAll("[.,]","");
            word = word.toLowerCase();
            //Stems the word before adding it
            result.add(stemmer.stem(word));
        }
        return result;
    }
}

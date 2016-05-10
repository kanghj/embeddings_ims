package sg.edu.nus.comp.nlp.ims.feature.embeddings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by kanghj on 13/2/16.
 */
public class FrequencyTrickedWordEmbeddings implements  WordEmbeddings{
    WordEmbeddings embeddings;

    public FrequencyTrickedWordEmbeddings(WordEmbeddings embddngs) throws IOException {
        Set<String> wordList = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("most_common_words_in_language/wiki-100k.txt"))) {
            String line = null;
            while ((line = reader.readLine()) != null ) {
                wordList.add(line.trim());
            }
        }

        System.out.println("num words in wordlist : " + wordList.size());

        Map<String, List<Double>> newModel = new HashMap<>();
        for (String allowedWord : wordList) {
            if (embddngs.getModel().keySet().contains(allowedWord)) {
                newModel.put(allowedWord, embddngs.getModel().get(allowedWord));
            }
        }
        embddngs.changeModel(newModel);
        this.embeddings = embddngs;
    }


    @Override
    public int numDimensions() {

        System.out.println("num dim : " + embeddings.numDimensions());
        return embeddings.numDimensions();
    }

    @Override
    public Map<String, List<Double>> getModel() {


        return embeddings.getModel();
    }

    @Override
    public void changeModel(Map<String, List<Double>> model) {
        this.embeddings.changeModel(model);
    }

    @Override
    public boolean isWordInVocab(String tokenInSentence) {

        System.out.println("num words in model : " + embeddings.getModel().size());
        return embeddings.isWordInVocab(tokenInSentence);
    }
}

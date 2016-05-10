package sg.edu.nus.comp.nlp.ims.feature.embeddings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SennaWordEmbeddings implements WordEmbeddings {
    protected final EmbeddingsPreprocessor embeddingsPreprocessor = new EmbeddingsPreprocessor(this);

    private Map<String, List<Double>> model = new LinkedHashMap<>();
    protected int numDimensions;

    protected static SennaWordEmbeddings instance;

    public static SennaWordEmbeddings instance() {
        if (instance == null) {
            SennaWordEmbeddings.instance = new SennaWordEmbeddings();
        }

        return instance;
    }

    protected SennaWordEmbeddings() {
        // load list of words
        String wordsListPath = "senna/hash/words.lst"; // TODO change to parameter
        BufferedReader wordListReader = null;
        try {
            wordListReader = new BufferedReader(new FileReader(wordsListPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        // load word embeddings from file
        String embeddingsPath = "senna/embeddings/embeddings.txt"; // TODO change to param
        BufferedReader embeddingsReader = null;
        try {
            embeddingsReader = new BufferedReader(new FileReader(embeddingsPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // read both files line by line
        try {
            String wordLine;
            while ((wordLine = wordListReader.readLine()) != null) {
                String embeddingsLine = embeddingsReader.readLine();
                String[] embeddings = embeddingsLine.split(" ");
                List<Double> dimensionValues = new ArrayList<>();

                for (String dimensionValue : embeddings) {
                    dimensionValues.add(Double.parseDouble(dimensionValue));
                }

                model.put(wordLine.intern(), dimensionValues);
                this.numDimensions = dimensionValues.size();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            embeddingsReader.close();
        } catch (IOException e) {
            System.out.println("err closing embeddings reader");
            e.printStackTrace();
        }
        //embeddingsPreprocessor.scaleDownEmbeddingsForDimensions(0.1);
        //scaleDownEmbeddings(0.1);
    }

    public List<Double> get(String token) {
        if (model.containsKey(token)) {
            return model.get(token);
        } else {
            throw new NoSuchElementException("word does not exist in model");
        }
    }

    public boolean isWordInVocab(String token) {
        return model.containsKey(token);
    }

    private void scaleDownEmbeddingsForDimensions(double sigma) {
        embeddingsPreprocessor.scaleDownEmbeddingsForDimensions(sigma);
    }


    private <K> double computeStandardDeviation(Map.Entry<K, List<Double>> wordEmbeddings) {


        return embeddingsPreprocessor.computeStandardDeviation(wordEmbeddings);
    }

    private double computeMean(List<Double> dimensionValues) {

        return embeddingsPreprocessor.computeMean(dimensionValues);
    }


    private double computeStandardDeviation(Map<String, List<Double>> wordEmbeddings) {

        return embeddingsPreprocessor.computeStandardDeviation(wordEmbeddings);
    }



    // temp for testing
    public static List<Double> minus(List<Double> first, List<Double> second) {
        assert first.size() == second.size();

        List<Double> result = new ArrayList<>(first.size());
        for (int i = 0; i < first.size(); i++) {
            result.add(first.get(i) - second.get(i));
        }
        return result;
    }
    // temp for testing
    public static List<Double> plus(List<Double> first, List<Double> second) {
        assert first.size() == second.size();

        List<Double> result = new ArrayList<>(first.size());
        for (int i = 0; i < first.size(); i++) {
            result.add( first.get(i) + second.get(i));
        }
        return result;
    }

    public static double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
            dotProduct += vectorA.get(i) * vectorB.get(i);
        }
        double result = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB) + 0.0001);
        result = result * result; // square to restrict from [0 .. 1]
        if (result < 0.0 || result > 1.0) {
            throw new RuntimeException("bad computation during cosine similarity. obtained " + result);
        }
        return result;
    }

    public Map<String, List<Double>> getModel() {
        return model;
    }

    @Override
    public void changeModel(Map<String, List<Double>> model) {
        this.model= model;
    }

    @Override
    public int numDimensions() {
        return this.numDimensions;
    }
}
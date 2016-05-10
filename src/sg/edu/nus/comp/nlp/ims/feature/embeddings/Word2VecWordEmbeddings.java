package sg.edu.nus.comp.nlp.ims.feature.embeddings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Word2VecWordEmbeddings implements  WordEmbeddings {
    private Map<String, List<Double>> model = new LinkedHashMap<>();
    public int numDimensions;

    protected final EmbeddingsPreprocessor embeddingsPreprocessor = new EmbeddingsPreprocessor(this);

    protected static Word2VecWordEmbeddings instance;

    public static Word2VecWordEmbeddings instance() {
        if (instance == null) {
            Word2VecWordEmbeddings.instance = new Word2VecWordEmbeddings();
        }

        return instance;
    }

    protected Word2VecWordEmbeddings() {
        // load list of words
        String wordsListPath = "word2vec2/words.lst"; // TODO change to parameter
        BufferedReader wordListReader = null;
        try {
            wordListReader = new BufferedReader(new FileReader(wordsListPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        // load word embeddings from file
        String embeddingsPath = "word2vec2/embeddings.txt"; // TODO change to param
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

                model.put(wordLine, dimensionValues);
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
        //scaleDownEmbeddingsForDimensions(0.1);


        //scaleDownEmbeddings(0.1);
    }

    public List<Double> get(String token) {
        if (model.containsKey(token)) {
            return model.get(token);
        } else {
            System.out.println("violating word is " + token);
            System.out.println("violating word ... " + model.containsKey(token));
            throw new NoSuchElementException("word does not exist in model");
        }
    }

    public boolean isWordInVocab(String token) {
        return model.containsKey(token);
    }

    private void scaleDownEmbeddings(double sigma) {

        //double stdev = computeStandardDeviation(model);
        for (Map.Entry<String, List<Double>> embeddings : model.entrySet()) {
            String word = embeddings.getKey();
            List<Double> values = embeddings.getValue();
            double stdev = computeStandardDeviation(embeddings);

            for (int i = 0; i < values.size(); i++) {
                Double newValue = values.get(i) * sigma / stdev;
                values.set(i, newValue);

            }
        }
    }

    private void scaleDownEmbeddingsForDimensions(double sigma) {

        //double stdev = computeStandardDeviation(model);

        double[] dimensionStdev = new double[this.numDimensions];
        Map<Integer, List<Double>> dimensionValues = new TreeMap<>();

        // first gather the word embedding values by dimension
        for (Map.Entry<String, List<Double>> embeddings : model.entrySet()) {
            List<Double> values = embeddings.getValue();

            for (int i = 0; i < values.size(); i++) {
                if (dimensionValues.get(i) == null ) {
                    dimensionValues.put(i, new ArrayList<Double>());
                }
                dimensionValues.get(i).add(values.get(i));
            }
        }

        for (Map.Entry<Integer, List<Double>> oneDim : dimensionValues.entrySet()) {
            dimensionStdev[oneDim.getKey()] = computeStandardDeviation(oneDim);
        }

        for (Map.Entry<String, List<Double>> embeddings : model.entrySet()) {
            List<Double> values = embeddings.getValue();

            for (int i = 0; i < values.size(); i++) {
                double stdev = dimensionStdev[i];

                Double newValue = values.get(i) * sigma / stdev;

                values.set(i, newValue);

            }
        }
    }


    private <K> double computeStandardDeviation(Map.Entry<K, List<Double>> wordEmbeddings) {
        assert !model.isEmpty();


        List<Double> values = wordEmbeddings.getValue();
        double mean = computeMean(values);

        double temp = 0;
        for (double a : values)
            temp += (mean-a)*(mean-a);

        double varianceOfWord = temp/values.size();

        return Math.sqrt(varianceOfWord);
    }

    private double computeMean(List<Double> dimensionValues) {
        double total = 0;
        for (Double val : dimensionValues) {
            total += val;
        }

        return total / dimensionValues.size();
    }


    private double computeStandardDeviation(Map<String, List<Double>> wordEmbeddings) {
        assert !model.isEmpty();

        double total = 0;
        int numItems = 0;
        for (Map.Entry<String, List<Double>> entry : wordEmbeddings.entrySet()) {
            for (Double value : entry.getValue()) {
                total += value;
                numItems ++;
            }
        }

        double mean = total / numItems;

        double temp = 0;
        for (Map.Entry<String, List<Double>> entry : wordEmbeddings.entrySet()) {
            for (Double value : entry.getValue()) {
                temp += (mean-value)*(mean-value);
            }
        }

        double variance = temp/numItems;

        return Math.sqrt(variance);
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
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    @Override
    public int numDimensions() {
        return this.numDimensions;
    }

    @Override
    public Map<String, List<Double>> getModel() {
        return this.model;
    }

    @Override
    public void changeModel(Map<String, List<Double>> model) {
        this.model = model;
    }
}
package sg.edu.nus.comp.nlp.ims.feature.embeddings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EmbeddingsPreprocessor {
    private final WordEmbeddings wordEmbeddings;

    public EmbeddingsPreprocessor(WordEmbeddings wordEmbeddings) {
        this.wordEmbeddings = wordEmbeddings;
    }

    void scaleDownEmbeddings(double sigma) {

        //double stdev = computeStandardDeviation(model);
        for (Map.Entry<String, List<Double>> embeddings : wordEmbeddings.getModel().entrySet()) {
            String word = embeddings.getKey();
            List<Double> values = embeddings.getValue();
            double stdev = computeStandardDeviation(embeddings);

            for (int i = 0; i < values.size(); i++) {
                Double newValue = values.get(i) * sigma / stdev;
                values.set(i, newValue);

            }
        }
    }

    void scaleDownEmbeddingsForDimensionsForMean(double mean) {


        double[] dimensionMean = new double[wordEmbeddings.numDimensions()];
        Map<Integer, List<Double>> dimensionValues = new TreeMap<Integer, List<Double>>();

        // first gather the word embedding values by dimension
        for (Map.Entry<String, List<Double>> embeddings : wordEmbeddings.getModel().entrySet()) {
            List<Double> values = embeddings.getValue();

            for (int i = 0; i < values.size(); i++) {
                if (dimensionValues.get(i) == null) {
                    dimensionValues.put(i, new ArrayList<Double>());
                }
                dimensionValues.get(i).add(values.get(i));
            }
        }

        for (Map.Entry<Integer, List<Double>> oneDim : dimensionValues.entrySet()) {
            dimensionMean[oneDim.getKey()] = 0;// computeMean(oneDim.getValue());
        }

        for (Map.Entry<String, List<Double>> embeddings : wordEmbeddings.getModel().entrySet()) {
            List<Double> values = embeddings.getValue();

            for (int i = 0; i < values.size(); i++) {
                Double newValue;
                if (dimensionMean[i] > 0.001) {
                    newValue = values.get(i) * mean / dimensionMean[i];
                } else {
                    newValue = values.get(i) + mean;
                }

                values.set(i, newValue);
            }
        }
    }

    void scaleDownEmbeddingsForDimensions(double sigma) {

        //double stdev = computeStandardDeviation(model);

        double[] dimensionStdev = new double[wordEmbeddings.numDimensions()];
        Map<Integer, List<Double>> dimensionValues = new TreeMap<Integer, List<Double>>();

        // first gather the word embedding values by dimension
        for (Map.Entry<String, List<Double>> embeddings : wordEmbeddings.getModel().entrySet()) {
            List<Double> values = embeddings.getValue();

            for (int i = 0; i < values.size(); i++) {
                if (dimensionValues.get(i) == null) {
                    dimensionValues.put(i, new ArrayList<Double>());
                }
                dimensionValues.get(i).add(values.get(i));
            }
        }

        for (Map.Entry<Integer, List<Double>> oneDim : dimensionValues.entrySet()) {
            dimensionStdev[oneDim.getKey()] = computeStandardDeviation(oneDim);
        }

        for (Map.Entry<String, List<Double>> embeddings : wordEmbeddings.getModel().entrySet()) {
            List<Double> values = embeddings.getValue();

            for (int i = 0; i < values.size(); i++) {
                double stdev = dimensionStdev[i];

                Double newValue = values.get(i) * sigma / stdev;

                values.set(i, newValue);

            }
        }
    }

    <K> double computeStandardDeviation(Map.Entry<K, List<Double>> wordEmbeddings) {
        assert !this.wordEmbeddings.getModel().isEmpty();


        List<Double> values = wordEmbeddings.getValue();
        double mean = computeMean(values);

        double temp = 0;
        for (double a : values)
            temp += (mean - a) * (mean - a);

        double varianceOfWord = temp / values.size();

        return Math.sqrt(varianceOfWord);
    }

    double computeMean(List<Double> dimensionValues) {
        double total = 0;
        for (Double val : dimensionValues) {
            total += val;
        }

        return total / dimensionValues.size();
    }

    double computeStandardDeviation(Map<String, List<Double>> wordEmbeddings) {
        assert !this.wordEmbeddings.getModel().isEmpty();

        double total = 0;
        int numItems = 0;
        for (Map.Entry<String, List<Double>> entry : wordEmbeddings.entrySet()) {
            for (Double value : entry.getValue()) {
                total += value;
                numItems++;
            }
        }

        double mean = total / numItems;

        double temp = 0;
        for (Map.Entry<String, List<Double>> entry : wordEmbeddings.entrySet()) {
            for (Double value : entry.getValue()) {
                temp += (mean - value) * (mean - value);
            }
        }

        double variance = temp / numItems;

        return Math.sqrt(variance);
    }

    static double computeMeanForSingleNestedMap(Map<String, Double> wordEmbeddings) {


        double total = 0;
        int numItems = 0;
        for (Map.Entry<String, Double> entry : wordEmbeddings.entrySet()) {
            total += entry.getValue();
            numItems++;
        }

        double mean = total / numItems;

       /* double temp = 0;
        for (Map.Entry<String, Double> entry : wordEmbeddings.entrySet()) {
            temp += (mean - entry.getValue()) * (mean - entry.getValue());
        }

        double variance = temp / numItems;*/

        return mean;
    }


}
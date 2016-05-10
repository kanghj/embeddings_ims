package sg.edu.nus.comp.nlp.ims.feature.embeddings;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to provide wrapper functions for the feature extractors that
 * are based on the entire embeddings and do not treat each dimension of the embedding
 * as a separate feature.
 *
 */
public class EmbeddingsSimilarityFeatureManager {
    public static File embeddingsPrototypeDirectory = getFile("prototype/");

    /*
     */
    public EmbeddingsSimilarityFeatureManager() throws IOException {
        if (!embeddingsPrototypeDirectory.isDirectory()) {
            throw new IOException(String.format(
                                    "Unable to construct EmbeddingsSimilarityFeatureManager. The directory %s should be created first!",
                                     embeddingsPrototypeDirectory.getAbsolutePath())
                                  );
        }

    }


    private static File getFile(String pathname) {
        return new File(pathname);
    }

    public List<Double> retrievePrototypeContextCentroid(String targetWord, String senseId) throws Exception {
        return retrievePrototypeFeature("context_centroid", targetWord, senseId);
    }

    public List<Double> retrievePrototypeFeature(String feature, String targetWord, String senseId)
            throws Exception {
        File prototypeContextSum = getFile(embeddingsPrototypeDirectory.getPath() + "/" + feature
                + "/" + targetWord + "/" + senseId);

        if (!prototypeContextSum.exists()) {
            throw new FileNotFoundException(
                    String.format("Unable to find the senseId embeddings example for %s, %s, %s", targetWord, senseId,prototypeContextSum)
            );
        }

        List<Double> embeddingValues = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(prototypeContextSum));
        String line;
        int fileLength = 0;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\\s");

            for (String token : tokens) {
                embeddingValues.add(
                        Double.valueOf(token)
                );
            }

            fileLength += 1;
        }

        reader.close();

        if (fileLength != 1 || embeddingValues.size() != 50) {
            if (fileLength != 1) {
                throw new Exception(String.format("Unexpected *file* length. Length should be 1, but is %d for %s, %s",
                        fileLength, targetWord, senseId)
                );
            } else {
                throw new Exception(String.format("Unexpected *embedding* length. Length should be 50, but is %d for %s, %s",
                        embeddingValues.size(), targetWord, senseId)
                );
            }
        }

        return embeddingValues;
    }

    public List<Double> retrievePrototypeContextSum(String targetWord, String senseId)
            throws Exception {
        File prototypeContextSum = getFile(embeddingsPrototypeDirectory.getPath() + "/context_sum"
                                           + "/" + targetWord + "/" + senseId);

        if (!prototypeContextSum.exists()) {
            throw new FileNotFoundException(
                        String.format("Unable to find the senseId embeddings example for %s, %s, %s", targetWord, senseId,prototypeContextSum)
                      );
        }

        List<Double> embeddingValues = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(prototypeContextSum));
        String line;
        int fileLength = 0;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\\s");

            for (String token : tokens) {
                embeddingValues.add(
                        Double.valueOf(token)
                );
            }

            fileLength += 1;
        }

        reader.close();

        if (fileLength != 1 || embeddingValues.size() != 50) {
            if (fileLength != 1) {
                throw new Exception(String.format("Unexpected *file* length. Length should be 1, but is %d for %s, %s",
                        fileLength, targetWord, senseId)
                );
            } else {
                throw new Exception(String.format("Unexpected *embedding* length. Length should be 50, but is %d for %s, %s",
                        embeddingValues.size(), targetWord, senseId)
                );
            }
        }

        return embeddingValues;
    }
}

package sg.edu.nus.comp.nlp.ims.feature.embeddings;

import sg.edu.nus.comp.nlp.ims.feature.IFeature;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class CSennaContextSumProductSetFeatureExtractor extends CSennaContextSumSimilarityFeatureExtractor {

    private int dimensionIndex;

    List<Double> instanceCentroid = new ArrayList<>();


    /**
     * get next feature, which is the element-wise product of the next dimension of this sense
     *
     * @return feature
     */
    public IFeature getNext() throws Exception {
        IFeature feature = null;

        if (this.instanceCentroid.isEmpty()) {
            //  first sum up the context sum of the current instance
            List<Double>  instanceContextSum = sumUpContextEmbeddings();

            // scale the embeddings down
            List<Double> instanceCentroid = new ArrayList<>();
            for (int i = 0; i < instanceContextSum.size(); i++) {
                instanceCentroid.add(instanceContextSum.get(i) / this.m_Sentence.size());
            }
            this.instanceCentroid = instanceCentroid;
        }

        // get possible senses
        //  first get target word
        String targetToken = this.m_Corpus.getValue(this.m_Index, "id").split("\\.")[0];


        //  then lookup the directories to find the filenames
        File targetWordSenseDirectory = new File(
                EmbeddingsSimilarityFeatureManager.embeddingsPrototypeDirectory.getCanonicalPath() + "/context_sum/" + targetToken);

        if (!targetWordSenseDirectory.exists() || !targetWordSenseDirectory.isDirectory()) {
            throw new FileNotFoundException();
        }


        List<File> possibleSensesFileList = Arrays.asList(
                targetWordSenseDirectory.listFiles()
        );

        // sort the senses alphabetically, to prevent mistakes due to sorting (which can happen in future)
        Collections.sort(possibleSensesFileList);

        if (this.senseIdIndex >= possibleSensesFileList.size()) {
            return null;
        }
        String currentSense = possibleSensesFileList.get(this.senseIdIndex).getName();

        List<Double> prototypeEmbeddings;
        try {
            prototypeEmbeddings = new EmbeddingsSimilarityFeatureManager().retrievePrototypeContextSum(targetToken, currentSense);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving prototype context sum for " + currentSense, e);
        }

        // obtain key of feature
        String key = formName(currentSense, this.dimensionIndex);


        if (instanceCentroid.size() != prototypeEmbeddings.size()) {
            throw new Exception(
                    "context sum dimensions differs from prototype dimensions! : context.size=" + instanceCentroid.size()
                            + " prototype.size = " + prototypeEmbeddings.size()
            );
        }

        double product = instanceCentroid.get(this.dimensionIndex) * prototypeEmbeddings.get(this.dimensionIndex);


        if (Double.isNaN(product)) {
            throw new Exception("Bad computation! product is NAN!!");
        }
        // TODO consider using binary feature instead of double feature
        feature = new CSennaContextFeature(key, product);

        // update index for next feature

        this.dimensionIndex += 1;
        if (this.dimensionIndex >= model.numDimensions()) {

            this.senseIdIndex += 1;
            this.dimensionIndex = 0;

            instanceCentroid = new ArrayList<>();
        }

        return feature;
    }


    private String formName( String currentSense, int dimIndex) {

        return ("CWProductSimilarity_" + currentSense + "_" + dimIndex).intern();
    }

    @Override
    public boolean restart() {
        this.dimensionIndex = 0;

        boolean result = super.restart();

        return result && !this.instanceCentroid.isEmpty();
    }

}

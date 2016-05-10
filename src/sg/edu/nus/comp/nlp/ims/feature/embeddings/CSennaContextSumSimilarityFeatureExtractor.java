package sg.edu.nus.comp.nlp.ims.feature.embeddings;

import sg.edu.nus.comp.nlp.ims.corpus.AItem;
import sg.edu.nus.comp.nlp.ims.corpus.ICorpus;
import sg.edu.nus.comp.nlp.ims.corpus.IItem;
import sg.edu.nus.comp.nlp.ims.corpus.ISentence;
import sg.edu.nus.comp.nlp.ims.feature.IFeature;
import sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor;
import sg.edu.nus.comp.nlp.ims.util.CSurroundingWordFilter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 *
 */
public class CSennaContextSumSimilarityFeatureExtractor implements IFeatureExtractor {
    // corpus to be extracted
    protected ICorpus m_Corpus = null;

    // index of current instance
    protected int m_Index = -1;

    // current sentence to process
    protected ISentence m_Sentence = null;

    // item index in current sentence
    protected int m_IndexInSentence;

    // item length
    protected int m_InstanceLength;


    // stop words filter
    protected CSurroundingWordFilter m_Filter = CSurroundingWordFilter.getInstance();

    // current feature
    protected IFeature m_CurrentFeature = null;

    protected int senseIdIndex = 0; //this is used to iterate over the possible senses


    protected WordEmbeddings model = SennaWordEmbeddings.instance();

    /**
     * constructor
     */
    public CSennaContextSumSimilarityFeatureExtractor() {

    }


    /*
     * (non-Javadoc)
     * @see sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor#getCurrentInstanceID()
     */
    @Override
    public String getCurrentInstanceID() {
        if (this.validIndex(this.m_Index)) {
            return this.m_Corpus.getValue(this.m_Index, "id");
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor#hasNext()
     */
    @Override
    public boolean hasNext() throws Exception {
        if (this.m_CurrentFeature != null) {
            return true;
        }
        if (this.validIndex(this.m_Index)) {
            this.m_CurrentFeature = this.getNext();
            if (this.m_CurrentFeature != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * get next feature, which is the cosine similarity of the current sum and the sample answer
     *
     * @return feature
     */
    public IFeature getNext() throws Exception {
        IFeature feature = null;

        // get possible senses
        //  first get target word
        //String targetToken = this.m_Sentence.getItem(this.m_IndexInSentence).get(AItem.Features.LEMMA.ordinal()).toLowerCase();
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
            prototypeEmbeddings = new EmbeddingsSimilarityFeatureManager()
                                        .retrievePrototypeContextSum(targetToken, currentSense);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving prototype context sum for " + currentSense, e);
        }

        // obtain key of feature
        String key = formName(currentSense);

        // compute the value, which is the cosine similarity
        //  first sum up the context sum of the current instance
        List<Double> instanceContextSum = sumUpContextEmbeddings();

        if (instanceContextSum.size() != prototypeEmbeddings.size()) {
            throw new Exception(
                        "context sum dimensions differs from prototype dimensions! : context.size=" + instanceContextSum.size()
                                + " prototype.size = " + prototypeEmbeddings.size()
                        );
        }

        double cosineSimilarity = cosineSimilarity(instanceContextSum.toArray(new Double[0]),
                                                   prototypeEmbeddings.toArray(new Double[0]));

       // System.out.println("cos sim : " + cosineSimilarity);
        if (Double.isNaN(cosineSimilarity)) {
            System.out.println(instanceContextSum);
            System.out.println(prototypeEmbeddings);
            throw new Exception("Bad computation! cos sim is NAN!!");
        }
        // TODO consider using binary feature instead of double feature
        feature = new CSennaContextFeature(key, cosineSimilarity * cosineSimilarity); // take square to limit to [0,1]

        // update index for next feature
        this.senseIdIndex += 1;

        return feature;
    }

    protected List<Double> sumUpContextEmbeddings() {
        List<Double> instanceContextSum = new ArrayList<>();
        for (int j = 0; j < model.numDimensions(); j++) {
            instanceContextSum.add(0.0);
        }

        for (int i = 0; i < this.m_Sentence.size(); i++) {
            if (i == m_IndexInSentence) {   // target word should be skipped
                continue;
            }

            IItem item = this.m_Sentence.getItem(i);
            String tokenInSentence = item.get(AItem.Features.TOKEN.ordinal());
            //System.out.print(lemmaInSentence + " : ");

            // drop stopwords
            //if (this.filter(item.get(AItem.Features.TOKEN.ordinal()))) {
            //    continue;
           // }

            if (model.isWordInVocab(tokenInSentence)) {
                List<Double> modelOutputGivenWord = model.getModel().get(tokenInSentence);

                for (int j = 0; j < modelOutputGivenWord.size(); j++) {
                    instanceContextSum.set(j, instanceContextSum.get(j) + modelOutputGivenWord.get(j));
                }
            } else {
                //System.out.print("not in model! ");


                for (int j = 0; j < model.numDimensions(); j++) {
                    instanceContextSum.set(j, instanceContextSum.get(j) + .0);
                }
            }
        }
       // System.out.println();

        return instanceContextSum;
    }

    private double cosineSimilarity(Double[] vector1, Double[] vector2) {
        double value = 0;
        double len1 = 0;
        double len2 = 0;

        for (int i = 0; i < vector1.length; i++) {

            value   += vector1[i] * vector2[i];
            len1    += vector1[i] * vector1[i];
            len2    += vector2[i] * vector2[i];
        }

        return value / (Math.sqrt(len1) * Math.sqrt(len2) + Double.MIN_VALUE);
    }

    private String formName( String currentSense) {
        return ("CWCosineSimilarity_" + currentSense).intern();
    }

    /*
     * (non-Javadoc)
     * @see sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor#next()
     */
    @Override
    public IFeature next() throws Exception {
        IFeature feature = null;
        if (this.hasNext()) {
            feature = this.m_CurrentFeature;
            this.m_CurrentFeature = null;
        }

        return feature;
    }

    /*
     * (non-Javadoc)
     * @see sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor#restart()
     */
    @Override
    public boolean restart() {
        this.m_CurrentFeature = null;
        this.senseIdIndex = 0;

        return this.validIndex(this.m_Index);
    }

    /*
     * (non-Javadoc)
     * @see sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor#setCorpus(sg.edu.nus.comp.nlp.ims.corpus.ICorpus)
     */
    @Override
    public boolean setCorpus(ICorpus p_Corpus) {
        if (p_Corpus == null) {
            return false;
        }
        this.m_Corpus = p_Corpus;

        this.m_Index = 0;
        this.restart();
        this.m_Index = -1;
        this.m_IndexInSentence = -1;
        this.m_InstanceLength = -1;
        return true;
    }

    /**
     * check the validity of index
     *
     * @param p_Index
     *            index
     * @return valid or not
     */
    protected boolean validIndex(int p_Index) {
        if (this.m_Corpus != null && this.m_Corpus.size() > p_Index
                && p_Index >= 0) {
            return true;
        }
        return false;
    }

    /**
     * check whether word is in stop word list or contains no alphabet
     *
     * @param p_Word
     *            word
     * @return true if it should be filtered, else false
     */
    public boolean filter(String p_Word) {
        return this.m_Filter.filter(p_Word);
    }

    /*
     * (non-Javadoc)
     * @see sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor#setCurrentInstance(int)
     */
    @Override
    public boolean setCurrentInstance(int p_Index) {
        if (this.validIndex(p_Index)) {
            this.m_Index = p_Index;
            this.m_IndexInSentence = this.m_Corpus.getIndexInSentence(p_Index);
            this.m_InstanceLength = this.m_Corpus.getLength(p_Index);
            int currentSent = this.m_Corpus.getSentenceID(p_Index);
            this.m_Sentence = this.m_Corpus.getSentence(currentSent);



            this.restart();
            return true;
        }
        return false;
    }

}

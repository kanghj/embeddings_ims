package sg.edu.nus.comp.nlp.ims.feature.embeddings;

import sg.edu.nus.comp.nlp.ims.corpus.AItem;
import sg.edu.nus.comp.nlp.ims.corpus.ICorpus;
import sg.edu.nus.comp.nlp.ims.corpus.IItem;
import sg.edu.nus.comp.nlp.ims.corpus.ISentence;
import sg.edu.nus.comp.nlp.ims.feature.IFeature;
import sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor;
import sg.edu.nus.comp.nlp.ims.util.CSurroundingWordFilter;

import java.util.ArrayList;
import java.util.List;

public class CSennaCollocationContextSumFeatureExtractor implements IFeatureExtractor {
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

    // the beginning positions of collocations
    protected ArrayList<Integer> m_Begins = new ArrayList<Integer>();

    // the ending positions of collocations
    protected ArrayList<Integer> m_Ends = new ArrayList<Integer>();


    // for extracting dimension values
    private int m_dimensionIndex = 0;

    // stop words filter
    protected CSurroundingWordFilter m_Filter = CSurroundingWordFilter.getInstance();

    // current feature
    protected IFeature m_CurrentFeature = null;

    protected int m_CollocationIndex;

    // lemma index
    protected static int g_LIDX = AItem.Features.LEMMA.ordinal();

    // token index
    protected static int g_TIDX = AItem.Features.TOKEN.ordinal();

    private WordEmbeddings model = ScaledWord2VecWordEmbeddings.instance();



    /**
     * constructor
     */
    public CSennaCollocationContextSumFeatureExtractor(WordEmbeddings embeddings) {
        this.model = embeddings;

        this.m_Begins.add(-2);
        this.m_Ends.add(-2);
        this.m_Begins.add(-1);
        this.m_Ends.add(-1);
        this.m_Begins.add(1);
        this.m_Ends.add(1);
        this.m_Begins.add(2);
        this.m_Ends.add(2);
        this.m_Begins.add(-2);
        this.m_Ends.add(-1);
        this.m_Begins.add(-1);
        this.m_Ends.add(1);
        this.m_Begins.add(1);
        this.m_Ends.add(2);
        this.m_Begins.add(-3);
        this.m_Ends.add(-1);
        this.m_Begins.add(-2);
        this.m_Ends.add(1);
        this.m_Begins.add(-1);
        this.m_Ends.add(2);
        this.m_Begins.add(1);
        this.m_Ends.add(3);
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
    public boolean hasNext() {
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
     * get next feature
     *
     * @return feature
     */
    private IFeature getNext() {
        IFeature feature = null;

        if (this.m_CollocationIndex >= 0
                && this.m_CollocationIndex < this.m_Begins.size()) {
            String key = this.formName(this.m_Begins.get(this.m_CollocationIndex),
                                        this.m_Ends.get(this.m_CollocationIndex),
                                        m_dimensionIndex);
            double value = this.sumDimensionValue(this.m_Begins.get(this.m_CollocationIndex),
                                                this.m_Ends.get(this.m_CollocationIndex),
                                                m_dimensionIndex);


            this.m_dimensionIndex++;
            if (this.m_dimensionIndex >=
                    model.numDimensions()) {
                this.m_dimensionIndex = 0;
                this.m_CollocationIndex += 1;
            }

            feature = new CSennaContextFeature(key, value);
            return feature;
        } else {
            return null;
        }
    }


    private double sumDimensionValue(int begin, int end, int dimension) {

        int currentSent = this.m_Corpus.getSentenceID(this.m_Index);

        IItem item = null;

        begin += this.m_IndexInSentence;
        end += this.m_IndexInSentence;

        double result = 0;
        for (int i = begin; i <= end; i++) { // <= end because the end is inclusive
            boolean isWithinSentence = i >= 0 && i < this.m_Sentence.size();
            if (isWithinSentence) {
                item = this.m_Sentence.getItem(i);
                String lemma = item.get(g_LIDX).toLowerCase();

                result += model.isWordInVocab(lemma) ? model.getModel().get(lemma).get(dimension)
                        : 0;//model.getModel().get("UNKNOWN").get(dimension);
            } else {
                result += 0;//model.getModel().get("PADDING").get(dimension);
            }
        }

        return result;
    }


    private String formName(int positionFromIndex, int positionEndIndex, int dimensionIndex) {
        return "CWCollocationSum_" + String.valueOf(positionFromIndex) + "_" + String.valueOf(positionEndIndex) + "_" + String.valueOf(dimensionIndex);
    }

    /*
     * (non-Javadoc)
     * @see sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor#next()
     */
    @Override
    public IFeature next() {
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

        this.m_CollocationIndex = 0;
        this.m_dimensionIndex = 0;

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


            String keyWord = null;
            int lower = this.m_Corpus.getLowerBoundary(currentSent);
            int upper = this.m_Corpus.getUpperBoundary(currentSent);
            /*for (int sentIdx = lower; sentIdx < upper; sentIdx++) {

                ISentence sentence = this.m_Corpus.getSentence(sentIdx);
                if (sentence != null) {

                }
            }*/
            this.restart();
            return true;
        }
        return false;
    }

}

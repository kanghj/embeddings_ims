package sg.edu.nus.comp.nlp.ims.feature.embeddings;

import sg.edu.nus.comp.nlp.ims.corpus.AItem;
import sg.edu.nus.comp.nlp.ims.corpus.ICorpus;
import sg.edu.nus.comp.nlp.ims.corpus.IItem;
import sg.edu.nus.comp.nlp.ims.corpus.ISentence;
import sg.edu.nus.comp.nlp.ims.feature.IFeature;
import sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor;
import sg.edu.nus.comp.nlp.ims.util.CSurroundingWordFilter;

import java.io.*;
import java.util.*;

public class CWordEmbeddingsSurroundingWordsFeatureExtractor implements IFeatureExtractor {
    public static final double SIMILARITY_TO_MATCH = 0.2;
    public static final double MAX_SIMILARITY_TO_MATCH = 1.00;
    private final int m_Right;
    private final int m_Left;
    // corpus to be extracted
    protected ICorpus m_Corpus = null;

    // index of current instance
    protected int m_Index = -1;

    // current sentence to process
    protected ISentence m_Sentence = null;


    // item index in current sentence
    protected int m_IndexInSentence;

    protected List<String> m_commonWords = new ArrayList<>();


    // item length
    protected int m_InstanceLength;


    // stop words filter
    protected CSurroundingWordFilter m_Filter = CSurroundingWordFilter.getInstance();

    // current feature
    protected IFeature m_CurrentFeature = null;

    protected int currentFeatureIndex = 0;
    protected List<IFeature> featureList = new ArrayList<>();

    // lemma index
    protected static int g_LIDX = AItem.Features.LEMMA.ordinal();

    // token index
    protected static int g_TIDX = AItem.Features.TOKEN.ordinal();

    protected WordEmbeddings model;


    /**
     * constructor
     */
    public CWordEmbeddingsSurroundingWordsFeatureExtractor(int p_Left, int p_Right) throws Exception {
        this(SennaWordEmbeddings.instance(), p_Left, p_Right);
    }

    public CWordEmbeddingsSurroundingWordsFeatureExtractor(WordEmbeddings embeddings, int p_Left, int p_Right) throws Exception {
        this.model = embeddings;

        this.m_Left = p_Left;
        this.m_Right = p_Right;

    }

    public CWordEmbeddingsSurroundingWordsFeatureExtractor(WordEmbeddings embeddings) throws Exception {
        this.model = embeddings;

        this.m_Left = Integer.MAX_VALUE;
        this.m_Right = Integer.MAX_VALUE;
    }

    public CWordEmbeddingsSurroundingWordsFeatureExtractor() throws Exception {
        this(SennaWordEmbeddings.instance());
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

     *
     * @return feature
     */
    public IFeature getNext() {

        if (this.currentFeatureIndex >= this.featureList.size()) {
            return null;
        } else {
            return this.featureList.get(this.currentFeatureIndex++);

        }



    }

    private void populateFeatureList(int currentSent) {
        /*
        for (int sentenceWordIndex = 0; sentenceWordIndex < this.m_Sentence.size(); sentenceWordIndex++ ) {
            IItem item = this.m_Sentence.getItem(sentenceWordIndex);
            String lemmaInSentence = item.get(AItem.Features.LEMMA.ordinal());

            if (this.filter(lemmaInSentence)) {
                continue;
            }
            if (!this.model.isWordInVocab(lemmaInSentence)) {
                continue;
            }

            List<Double> sentenceWordEmbeddings = this.model.getModel().get(lemmaInSentence);

            for (String commonWord : this.m_commonWords) {
                List<Double> commonWordEmbeddings = this.model.getModel().get(commonWord);
                if (SennaWordEmbeddings.cosineSimilarity(commonWordEmbeddings, sentenceWordEmbeddings) > SIMILARITY_TO_MATCH) {
                    IFeature feature = new CEmbeddingSurroundingWord();
                    feature.setKey(formName(commonWord));

                    this.featureList.add(feature);
                }
            }
        }*/



        Set<String> featureHasBeenActivated = new HashSet<>();

        int lower = this.m_Corpus.getLowerBoundary(currentSent);
        int upper = this.m_Corpus.getUpperBoundary(currentSent);
        for (int sentIdx = lower; sentIdx < upper; sentIdx++) {
            if (currentSent - sentIdx > this.m_Left
                    || sentIdx - currentSent > this.m_Right) {
                continue;
            }
            ISentence sentence = this.m_Corpus.getSentence(sentIdx);
            if (sentence != null) {
                for (int sentenceWordIndex = 0; sentenceWordIndex < sentence.size(); sentenceWordIndex++ ) {
                    IItem item = sentence.getItem(sentenceWordIndex);

                    if (this.filter(item.get(AItem.Features.TOKEN.ordinal()))) {
                        continue;
                    }

                    String lemmaInSentence = item.get(AItem.Features.LEMMA.ordinal());

                    if (!this.model.isWordInVocab(lemmaInSentence)) {
                        /*for (String commonWord : this.m_commonWords) {
                            if (lemmaInSentence.equals(commonWord) && (!hasFeatureBeenActivated(featureHasBeenActivated, commonWord))) {
                                System.out.println("word match!");
                                activateFeature(featureHasBeenActivated, commonWord);
                            }
                        }*/
                        continue;
                    }

                    List<Double> sentenceWordEmbeddings = this.model.getModel().get(lemmaInSentence);

                    for (String commonWord : this.m_commonWords) {
                        /*if (lemmaInSentence.equals(commonWord) && (!hasFeatureBeenActivated(featureHasBeenActivated, commonWord))) {
                            System.out.println("word match!");
                            activateFeature(featureHasBeenActivated, commonWord);
                            continue;
                        }*/

                        if (model.isWordInVocab(commonWord)) {
                            List<Double> commonWordEmbeddings = this.model.getModel().get(commonWord);

                            Double cosineSimilarity = SennaWordEmbeddings.cosineSimilarity(commonWordEmbeddings, sentenceWordEmbeddings);
                            if (//!hasFeatureBeenActivated(featureHasBeenActivated, commonWord)
                            //        &&
                                    cosineSimilarity > SIMILARITY_TO_MATCH
                                    && cosineSimilarity < MAX_SIMILARITY_TO_MATCH
                                    && !commonWord.equals(lemmaInSentence)
                                    ) {
                               // System.out.println("similarity match for " + lemmaInSentence + " matches" + commonWord);
                                activateFeature(featureHasBeenActivated, commonWord,
                                                cosineSimilarity);
                            }

                        }

                    }

                }
            }
        }

    }

    private boolean hasFeatureBeenActivated(Set<String> featureHasBeenActivated, String commonWord) {
        return featureHasBeenActivated.contains(commonWord);
    }

    private void activateFeature(Set<String> featureHasBeenActivated, String commonWord, Double similarity) {
        CEmbeddingSurroundingWord feature = new CEmbeddingSurroundingWord();
        feature.setKey(formName(commonWord));
        //feature.setValue(String.valueOf(similarity));


        featureHasBeenActivated.add(commonWord);

        this.featureList.add(feature);
    }


    private String formName(String word) {
        return ("CWSurroundingWord_" + word).intern();
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
        this.m_commonWords.clear();


        String targetToken = this.m_Corpus.getValue(this.m_Index, "id").split("\\.")[0];


        //  then lookup the directories to find the filenames

        File commonWordsFileForToken = new File("common_words/" + targetToken);

        if (!commonWordsFileForToken.exists() ) {
            throw new RuntimeException("common word files not found!");
        }

        {
            try {
                //System.out.println("gonna read from " + commonWordsFileForToken);
                String line;

                try(BufferedReader inputFromFile = new BufferedReader(new FileReader(commonWordsFileForToken))) {

                    while ((line = inputFromFile.readLine()) != null) {
                        if (line.split(" ").length != 1) {
                            throw new Exception("incorrect number of words in " + commonWordsFileForToken);
                        }


                        m_commonWords.add(line);

                    }
                }
                //System.out.println("finish read from " + commonWordsFileForToken);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }



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

            this.populateFeatureList(currentSent);

            return true;
        }

        return false;
    }

}

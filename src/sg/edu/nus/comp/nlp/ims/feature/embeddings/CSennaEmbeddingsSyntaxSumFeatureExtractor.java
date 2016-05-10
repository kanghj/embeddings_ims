package sg.edu.nus.comp.nlp.ims.feature.embeddings;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import opennlp.tools.lang.english.TreebankParser;
import opennlp.tools.parser.Parse;

import opennlp.tools.parser.ParserME;
import sg.edu.nus.comp.nlp.ims.corpus.AItem;
import sg.edu.nus.comp.nlp.ims.corpus.ICorpus;
import sg.edu.nus.comp.nlp.ims.corpus.IItem;
import sg.edu.nus.comp.nlp.ims.corpus.ISentence;
import sg.edu.nus.comp.nlp.ims.feature.IFeature;
import sg.edu.nus.comp.nlp.ims.feature.IFeatureExtractor;
import sg.edu.nus.comp.nlp.ims.util.CSurroundingWordFilter;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Returns features of each dimension of ()
 */
public class CSennaEmbeddingsSyntaxSumFeatureExtractor implements IFeatureExtractor {
    private OpenNLPParser parser;

    // corpus to be extracted
    protected ICorpus m_Corpus = null;

    // index of current instance
    protected int m_Index = -1;

    // current sentence to process
    protected ISentence m_Sentence = null;


    private List<Double> processedFinalEmbeddingsSum = null;


    // item index in current sentence
    protected int m_IndexInSentence;

    // item length
    protected int m_InstanceLength;

    protected int m_dimensionIndex = 0;


    // stop words filter
    protected CSurroundingWordFilter m_Filter = CSurroundingWordFilter.getInstance();

    // current feature
    protected IFeature m_CurrentFeature = null;

    // lemma index
    protected static int g_LIDX = AItem.Features.LEMMA.ordinal();

    // token index
    protected static int g_TIDX = AItem.Features.TOKEN.ordinal();

    protected SennaWordEmbeddings model = ScaledSennaWordEmbeddings.instance();


    /**
     * constructor
     */
    public CSennaEmbeddingsSyntaxSumFeatureExtractor() throws Exception {
        parser = new OpenNLPParser(new File("resources/opennlp_models/parser/") );
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
    public IFeature getNext() {
        IFeature feature = null;

        if (this.m_dimensionIndex >= model.numDimensions) {
            System.out.println("end of one round of getting dimensions");
            return null;
        }

        if (this.processedFinalEmbeddingsSum == null) {
            // compute...
            ParseTraverser traverser = new ParseTraverser(this.model);

            Parse sentenceParse;
            try {
                OpenNLPParser parser = new OpenNLPParser(new File("resources/opennlp_models/parser/") );

                List<String> tokensInSentence = new ArrayList<>();
                for (int i = 0; i < this.m_Sentence.size(); i++) {
                    IItem item = this.m_Sentence.getItem(i);
                    String token = item.get(AItem.Features.TOKEN.ordinal());
                    tokensInSentence.add(token);
                }

                String sentenceAsString = tokensInSentence.stream().collect(Collectors.joining(" "));

                sentenceParse = parser.parseSentence(sentenceAsString);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Unable to initalise parser", e);
            }

            this.processedFinalEmbeddingsSum = traverser.getDimensionsOfFinalResult(sentenceParse);
            if (this.processedFinalEmbeddingsSum == null) {
                throw new RuntimeException("can't be null here!");
            }
        }

        // obtain key of feature
        String key = formName(m_dimensionIndex);
        double totalValue = this.processedFinalEmbeddingsSum.get(this.m_dimensionIndex);

        feature = new CSennaContextFeature(key, totalValue);

        // update indexes for next feature
        m_dimensionIndex += 1;

        return feature;
    }

    private String formName( int dimensionIndex) {
        return ("CWSyntaxTanh_" + String.valueOf(dimensionIndex)).intern();
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
        this.m_dimensionIndex = 0;
        this.processedFinalEmbeddingsSum = null;

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


    private static class OpenNLPParser {
        private ParserME parser;

        public OpenNLPParser(File modelPath) throws Exception {
            this.parser = TreebankParser.getParser(modelPath.getPath());


        }

        public Parse parseSentence(String tokenizedSentence) {
            return TreebankParser.parseLine(tokenizedSentence, parser, 1)[0];
        }

        public Parse[] getParses(String tokenizedSentence, int N) {
            return TreebankParser.parseLine(tokenizedSentence, parser, N);
        }
    }

    private static List<Double> tanH(List<Double> initialValues) {
        List<Double> tanHValues = new ArrayList<>();
        for (Double initialValue : initialValues) {
            tanHValues.add(Math.tanh(initialValue));
        }
        return initialValues.stream().map(initialVal -> Math.tanh(initialVal))
                                     .collect(Collectors.toList());
    }

    private static class ParseTraverser {
        private WordEmbeddings embeddings;
        private enum ParseStrategy {
            TANH, CONCAT
        }
        private ParseStrategy strategy = ParseStrategy.TANH;

        public ParseTraverser(WordEmbeddings embeddings) {
            this.embeddings = embeddings;
            System.out.println("initailised with model size " + embeddings.numDimensions());
        }


        public List<Double> getDimensionsOfFinalResult(Parse currentWord) {
            List<Double> result = new ArrayList<>(embeddings.numDimensions());
            for (int i = 0; i < embeddings.numDimensions(); i++) {
                result.add(0.0);
            }

            Parse[] childParses = currentWord.getChildren();
            if (childParses.length > 0) {
               // System.out.println("::: not bottom  " + result.size());
               // System.out.println(currentWord != null ? currentWord.getHead()
                //                                        : "currentword is null");
                for (Parse child : childParses) {
                    result = SennaWordEmbeddings.plus(result, getDimensionsOfFinalResult(child));
                }

                if (strategy == ParseStrategy.TANH) {
                    result = tanH(result);
                } else {
                    throw new InvalidParameterException("Strategy not implemented");
                }
                return result;
            } else  {
                String processedWord = currentWord.getHead().toString().toLowerCase();
                //System.out.println("::: " + processedWord);
                if (embeddings.isWordInVocab(processedWord)) {
                    List<Double> embeddingsOfCurrentWord = embeddings.getModel().get(processedWord);

                    if (strategy == ParseStrategy.TANH) {
                      //  System.out.println("in vocab " + embeddingsOfCurrentWord.size());
                        return tanH(embeddingsOfCurrentWord);
                    } else {
                        throw new InvalidParameterException("Strategy not implemented");
                    }
                } else {

                    //System.out.println("not in vocab " + result.size());
                    return result;
                }
            }
        }

    }


    public static void main(String[] args) throws Exception {

        long startTime = System.nanoTime();
        OpenNLPParser parser = new OpenNLPParser(new File("resources/opennlp_models/parser/") );

        //String[] str = {"The store assistant updates the rental list.", "I can almost always tell when movies use fake dinosaurs."};
        String[] str = {"Paintings , drawings and sculpture from every period of art during the last 350 years will be on display ranging from a Tudor portrait to contemporary British art ."};
        Parse parse = parser.parseSentence(str[0]);

        System.out.println("> "+parse.getChildren()[0].getHead());
        System.out.println(">"+parse.getChildren()[0].getChildren()[0].getHead().toString());
        System.out.println(parse.getChildren()[0].getChildren()[0].getChildCount());
        System.out.println(parse.getChildren()[0].getChildren()[0].getDerivation());
        System.out.println(parse.getChildren()[0].getChildren()[0].getType());
        System.out.println(parse.getChildren()[0].getChildren()[0].getSpan());
        System.out.println(parse.getChildren()[0].getChildren()[0].getTagNodes());
        System.out.println(parse.getChildren()[0].getChildren()[0].getChildren()[0].getType());
        System.out.println(parse.getChildren()[0].getChildren()[0].getChildren()[1].getType());


        System.out.println("normal parse completed in "  + ((System.nanoTime() - startTime) / 10000000));


        String modelPath = DependencyParser.DEFAULT_MODEL;

        DependencyParser p = DependencyParser.loadFromModelFile(modelPath);

        String pathToModel = "resources/english-left3words-distsim.tagger";
        System.out.println(pathToModel);
        MaxentTagger tagger = new MaxentTagger(pathToModel);

        for (String text : str) {
            DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
            for (List<HasWord> sentence : tokenizer) {
                System.out.println(sentence);
                List<TaggedWord> tagged = tagger.tagSentence(sentence);
                System.out.println(tagged);

                GrammaticalStructure gs = p.predict(tagged);

                // Print typed dependencies
                System.err.println(gs);

                Collection<TypedDependency> typedDependencies = gs.typedDependenciesCollapsedTree();


                //System.out.println(gs.typedDependenciesCollapsedTree());


                for (TypedDependency td : typedDependencies) {
                    //if (td.reln().equals(EnglishGrammaticalRelations.NOMINAL_SUBJECT)) {
                    System.out.println(td);

                    System.out.println(td.reln().getLongName().toLowerCase().contains("subject"));


                    System.out.println(td.gov().backingLabel().value());
                    System.out.println(td.dep().backingLabel().value());


                    //}
                }
                System.out.println("====");

            }
        }
        System.out.println("dep parse completed in "  + ((System.nanoTime() - startTime) / 10000000) );


    }
}

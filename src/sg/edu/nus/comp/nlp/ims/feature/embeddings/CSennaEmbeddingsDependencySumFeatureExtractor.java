package sg.edu.nus.comp.nlp.ims.feature.embeddings;

import edu.stanford.nlp.ling.CoreLabel;
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

import java.io.File;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Returns features of each dimension of ()
 */
public class CSennaEmbeddingsDependencySumFeatureExtractor implements IFeatureExtractor {
    private OpenNLPParser parser;

    // corpus to be extracted
    protected ICorpus m_Corpus = null;

    // index of current instance
    protected int m_Index = -1;

    // current sentence to process
    protected ISentence m_Sentence = null;


    private List<Double> processedFinalSubjectEmbeddingsSum = null;
    private List<Double> processedFinalObjectEmbeddingsSum = null;


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
    public CSennaEmbeddingsDependencySumFeatureExtractor() throws Exception {
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

        if (this.processedFinalObjectEmbeddingsSum == null || this.processedFinalSubjectEmbeddingsSum == null) {
            // compute...
            String modelPath = DependencyParser.DEFAULT_MODEL;

            DependencyParser p = DependencyParser.loadFromModelFile(modelPath);

            String pathToModel = "resources/english-left3words-distsim.tagger";
            MaxentTagger tagger = new MaxentTagger(pathToModel);

            List<String> tokensInSentence = new ArrayList<>();
            for (int i = 0; i < this.m_Sentence.size(); i++) {
                IItem item = this.m_Sentence.getItem(i);
                String token = item.get(AItem.Features.TOKEN.ordinal());
                tokensInSentence.add(token);
            }

            String sentenceAsString = tokensInSentence.stream().collect(Collectors.joining(" "));

            DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(sentenceAsString));

            // only one sentence, so just get the first output of the iterator
            List<TaggedWord> tagged = tagger.tagSentence(tokenizer.iterator().next());
            GrammaticalStructure gs = p.predict(tagged);

            DependencyParseTraverser traverser = new DependencyParseTraverser(this.model, gs);




        }

        // obtain key of feature
        String key = formName(m_dimensionIndex);
        //double totalValue = this.processedFinalEmbeddingsSum.get(this.m_dimensionIndex);

        feature = new CSennaContextFeature(key, 0);

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
        this.processedFinalSubjectEmbeddingsSum = null;
        this.processedFinalObjectEmbeddingsSum = null;

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

    private static class DependencyParseTraverser {
        private WordEmbeddings embeddings;
        Collection<TypedDependency> typedDependencies;
        private Map<Integer, List<Integer>> positionLinks = new HashMap<>();
        private Map<Integer, TypedDependency> startIndexToDependency = new HashMap<>();

        public DependencyParseTraverser(WordEmbeddings embeddings, GrammaticalStructure gs) {
            this.embeddings = embeddings;
            Collection<TypedDependency> typedDependencies = gs.typedDependenciesCollapsedTree();
            System.out.println("initailised with model size " + embeddings.numDimensions());
        }

        public List<Double> getObjectSum() {
            return getSum("object");
        }

        public List<Double> getSubjectSum() {
            return getSum("subject");
        }
        private List<Double> getSum(String type) {
            // find the  link

            int indexOfSubjectRoot = -999;
            for (TypedDependency td : typedDependencies) {
                if (td.reln().getLongName().toLowerCase().contains(type)) {
                    indexOfSubjectRoot = td.dep().backingLabel().index();
                }
                startIndexToDependency.put(td.gov().backingLabel().index(), td);
            }

            if (indexOfSubjectRoot == -999) {
                // unable to find...
                return new ArrayList<Double>(Collections.nCopies(60, 0.0));
            }


            // use subject root to get all tokens in subject
            Stack<String> tokensInSubject = new Stack<>();
            Stack<Integer> frontierOfTraversal = new Stack<>();
            frontierOfTraversal.push(indexOfSubjectRoot);
            while (!frontierOfTraversal.isEmpty()) {
                int currentIndex = frontierOfTraversal.pop();
                tokensInSubject.push(startIndexToDependency.get(currentIndex).gov().backingLabel().value());

                // get children
                for (int childIndex : positionLinks.get(currentIndex)) {
                    frontierOfTraversal.push(childIndex);
                }
            }

            List<String> processedTokens = tokensInSubject.stream().map((x) -> x.toLowerCase())
                                            .collect(Collectors.toList());
            List<Double> result = new ArrayList<>();
            for (String token : processedTokens) {
                List<Double> wordEmbeddings = embeddings.getModel().containsKey(token) ?
                                                embeddings.getModel().get(token) :
                                                new ArrayList<Double>(Collections.nCopies(60, 0.0));
                result = SennaWordEmbeddings.plus(result, wordEmbeddings);
            }

            return result;
        }


        /**
         * Root is 0
         * @return
         */
        public Map<Integer, List<Integer>> getConnections() {

            for (TypedDependency td : typedDependencies) {
                int startIndex = td.gov().backingLabel().index();
                int endIndex = td.dep().backingLabel().index();

                if (!positionLinks.containsKey(startIndex)) {
                    positionLinks.put(startIndex, new ArrayList<>());
                }
                positionLinks.get(startIndex).add(endIndex);

            }

            return  positionLinks;
        }

    }


    public static void main(String[] args) throws Exception {
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


                    System.out.println(td.gov().backingLabel().value()); System.out.println(td.gov().backingLabel().toString(CoreLabel.OutputFormat.VALUE_INDEX));
                    System.out.println(td.dep().backingLabel().value()); System.out.println(td.dep().backingLabel().index());


                    //}
                    System.out.println("====");
                }

            }
        }


    }
}

package sg.edu.nus.comp.nlp.ims.feature.embeddings;

import sg.edu.nus.comp.nlp.ims.corpus.AItem;
import sg.edu.nus.comp.nlp.ims.corpus.IItem;
import sg.edu.nus.comp.nlp.ims.feature.IFeature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class CWordEmbeddingsWeightedSentenceSumFeatureExtractor
                extends CWordEmbeddingsSentenceSumFeatureExtractor {

    Map<String, Double> docInverseFreq = new HashMap<>();


    private CWordEmbeddingsWeightedSentenceSumFeatureExtractor(Map<String, Integer> documentFrequencies) {
        double estimatedTotalDocumentsNum = documentFrequencies.get("the").doubleValue();

        for (Map.Entry<String, Integer> tokenAndCount : documentFrequencies.entrySet()) {

            Double idf = Math.log(estimatedTotalDocumentsNum / (1 + tokenAndCount.getValue()));
            docInverseFreq.put(tokenAndCount.getKey(), idf);
        }

        scaleIdf(2.0);
    }

    /**
     * Scale the IDF values to a target,
     * same reasoning as the scaling of word embeddings, to prevent this from getting much heavier weights
     * than the other features
     * @param targetMean
     */
    private void scaleIdf(double targetMean) {

        double mean = EmbeddingsPreprocessor.computeMeanForSingleNestedMap(docInverseFreq);

        System.out.println("mean : " + mean);

        for (Map.Entry<String, Double> invDocFreq : docInverseFreq.entrySet()) {
            Double newValue = invDocFreq.getValue() * targetMean / mean;

            docInverseFreq.put(invDocFreq.getKey(), newValue);
        }

    }

    public static CWordEmbeddingsWeightedSentenceSumFeatureExtractor produceWeightedSumExtractorUsing(File weightsFile,
                                                                                                      File wordlstFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(weightsFile));
        BufferedReader wordLstreader = new BufferedReader(new FileReader(wordlstFile));

        Map<String, Integer> docFreqs = new HashMap<>();

        String line;
        while ((line = reader.readLine()) != null) {
            String word = wordLstreader.readLine().trim();

            int docFreq = Integer.valueOf(line);
            docFreqs.put(word, docFreq);
        }

        return new CWordEmbeddingsWeightedSentenceSumFeatureExtractor(docFreqs);
    }

    @Override
    public IFeature getNext() {
        IFeature feature = null;

        if (this.m_dimensionIndex >= model.numDimensions()) {
            return null;
        }

        // obtain key of feature
        String key = formName(m_dimensionIndex);
        double totalValue = 0;


        for (int i = 0; i < this.m_Sentence.size(); i++) {
            if (i == m_IndexInSentence) {   // target word should be skipped
                continue;
            }

            IItem item = this.m_Sentence.getItem(i);
            String lemmaInSentence = item.get(AItem.Features.LEMMA.ordinal());
            if (this.filter(item.get(AItem.Features.TOKEN.ordinal()))) {
                continue;
            }


            double value = 0;
            double scaledValue = 0;
            if (model.isWordInVocab(lemmaInSentence)) {
                List<Double> modelOutputGivenWord = model.getModel().get(lemmaInSentence);
                value = modelOutputGivenWord.get(m_dimensionIndex);

                scaledValue = docInverseFreq.get(lemmaInSentence) * value;
            }


            if (this.m_dimensionIndex == 0) {
                System.out.println(lemmaInSentence + "," + " idf =" + docInverseFreq.get(lemmaInSentence) + ", "
                                   + value + " -> " + scaledValue);
            }


            totalValue += scaledValue;
        };

        feature = new CSennaContextFeature(key, totalValue);

        // update indexes for next feature
        m_dimensionIndex += 1;

        return feature;
    }

    protected String formName( int dimensionIndex) {
        return ("CWWeightedVector_" + String.valueOf(dimensionIndex)).intern();
    }




}

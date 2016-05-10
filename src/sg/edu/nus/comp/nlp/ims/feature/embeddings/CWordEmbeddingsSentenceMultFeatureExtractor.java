package sg.edu.nus.comp.nlp.ims.feature.embeddings;

/**
 * Created by kanghj on 16/1/16.
 */
public class CWordEmbeddingsSentenceMultFeatureExtractor extends CWordEmbeddingsSentenceSumFeatureExtractor {

    public CWordEmbeddingsSentenceMultFeatureExtractor(WordEmbeddings instance) {
        this.model = instance;
    }

    @Override
    protected double updateValueForDimensionFeature(double accumulatedValue, double nextValue) {
        return nextValue > 0.001 ? accumulatedValue * nextValue
                                 : accumulatedValue;
    }

    @Override
    protected double getStartValue() {
        return 1.0;
    }

    protected String formName( int dimensionIndex) {
        return ("CWMult_" + String.valueOf(dimensionIndex)).intern();
    }
}

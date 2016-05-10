package sg.edu.nus.comp.nlp.ims.feature.embeddings;

/**
 * Created by kanghj on 22/2/16.
 */
public class SizeScaledDependencyWordEmbeddings extends DependencyWordEmbeddings {
    public static DependencyWordEmbeddings instance() {
        if (instance == null) {
            instance = new SizeScaledDependencyWordEmbeddings(0.05);
        }

        return instance;
    }

    private SizeScaledDependencyWordEmbeddings(double alpha) {
        super(alpha);
        embeddingsPreprocessor.scaleDownEmbeddingsForDimensions(0.05);
        embeddingsPreprocessor.scaleDownEmbeddingsForDimensionsForMean(1.0);

    }


}

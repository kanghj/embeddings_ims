package sg.edu.nus.comp.nlp.ims.feature.embeddings;

/**
 * Created by kanghj on 19/12/15.
 */
public class ScaledDependencyWordEmbeddings extends DependencyWordEmbeddings {

    public static DependencyWordEmbeddings instance() {
        if (instance == null) {
            DependencyWordEmbeddings.instance = new ScaledDependencyWordEmbeddings(0.05);
        }



        return instance;
    }

    private ScaledDependencyWordEmbeddings(double alpha) {
        super(alpha);

        embeddingsPreprocessor.scaleDownEmbeddingsForDimensions(0.1);

    }

}

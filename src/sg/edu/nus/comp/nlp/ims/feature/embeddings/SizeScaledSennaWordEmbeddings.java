package sg.edu.nus.comp.nlp.ims.feature.embeddings;

/**
 * Created by kanghj on 22/2/16.
 */
public class SizeScaledSennaWordEmbeddings extends SennaWordEmbeddings {
    public static SennaWordEmbeddings instance() {
        if (instance == null) {
            instance = new SizeScaledSennaWordEmbeddings();
        }



        return instance;
    }

    private SizeScaledSennaWordEmbeddings() {
        super();

        embeddingsPreprocessor.scaleDownEmbeddingsForDimensions(0.05);
        embeddingsPreprocessor.scaleDownEmbeddingsForDimensionsForMean(1.0);

    }


}

package sg.edu.nus.comp.nlp.ims.feature.embeddings;

/**
 * Created by kanghj on 22/2/16.
 */
public class SizeScaledGloveWordEmbeddings extends GloveWordEmbeddings {
    public static GloveWordEmbeddings instance() {
        if (instance == null) {
            instance = new SizeScaledGloveWordEmbeddings();
        }



        return instance;
    }

    private SizeScaledGloveWordEmbeddings() {
        super();
        embeddingsPreprocessor.scaleDownEmbeddingsForDimensions(0.05);
        embeddingsPreprocessor.scaleDownEmbeddingsForDimensionsForMean(1.0);

    }


}

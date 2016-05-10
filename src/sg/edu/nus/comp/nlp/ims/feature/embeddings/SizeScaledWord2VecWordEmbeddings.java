package sg.edu.nus.comp.nlp.ims.feature.embeddings;

/**
 * Created by kanghj on 22/2/16.
 */
public class SizeScaledWord2VecWordEmbeddings extends Word2VecWordEmbeddings {
    public static Word2VecWordEmbeddings instance() {
        if (instance == null) {
            instance = new SizeScaledWord2VecWordEmbeddings();
        }



        return instance;
    }

    private SizeScaledWord2VecWordEmbeddings() {
        super();


        embeddingsPreprocessor.scaleDownEmbeddingsForDimensions(0.05);
        embeddingsPreprocessor.scaleDownEmbeddingsForDimensionsForMean(1.0);

    }


}

package sg.edu.nus.comp.nlp.ims.feature.embeddings;

/**
 * Created by kanghj on 4/2/16.
 */
public class ScaledWord2VecWordEmbeddings extends Word2VecWordEmbeddings {
    public static Word2VecWordEmbeddings instance() {
        if (instance == null) {
            Word2VecWordEmbeddings.instance = new ScaledWord2VecWordEmbeddings();
        }



        return instance;
    }

    private ScaledWord2VecWordEmbeddings() {
        super();

        embeddingsPreprocessor.scaleDownEmbeddingsForDimensions(0.1);
       // embeddingsPreprocessor.scaleDownEmbeddings(1.0);

    }
}

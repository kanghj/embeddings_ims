package sg.edu.nus.comp.nlp.ims.feature.embeddings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kanghj on 19/12/15.
 */
public class ScaledSennaWordEmbeddings extends SennaWordEmbeddings {
    private static ScaledSennaWordEmbeddings instance;

    public static SennaWordEmbeddings instance() {
        if (instance == null) {
            instance = new ScaledSennaWordEmbeddings();
        }

        

        return instance;
    }

    private ScaledSennaWordEmbeddings() {
        super();

        embeddingsPreprocessor.scaleDownEmbeddingsForDimensions(0.1);

    }

}

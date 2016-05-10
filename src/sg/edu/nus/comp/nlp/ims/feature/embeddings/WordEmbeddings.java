package sg.edu.nus.comp.nlp.ims.feature.embeddings;

import java.util.List;
import java.util.Map;

/**
 * Created by kanghj on 13/12/15.
 */
public interface WordEmbeddings {
    public int numDimensions();

    public Map<String, List<Double>> getModel();
    public void changeModel(Map<String, List<Double>> model);

    boolean isWordInVocab(String tokenInSentence);
}


package sg.edu.nus.comp.nlp.ims.feature.embeddings;

import sg.edu.nus.comp.nlp.ims.feature.ABinaryFeature;
import sg.edu.nus.comp.nlp.ims.feature.CDoubleFeature;
import sg.edu.nus.comp.nlp.ims.feature.IFeature;

/**
 * Created by kanghj on 20/1/16.
 */
public class CEmbeddingSurroundingWord extends ABinaryFeature {

    /**
     *
     */
    private static final long serialVersionUID = 12313123232332322L;

    /**
     * constructor
     */
    public CEmbeddingSurroundingWord() {
        this.m_Key = null;

    }

    /*
     * (non-Javadoc)
     * @see sg.edu.nus.comp.nlp.ims.feature.ABinaryFeature#clone()
     */
    public Object clone() {
        CEmbeddingSurroundingWord clone = new CEmbeddingSurroundingWord();
        clone.m_Key = this.m_Key;
        return clone;
    }
}

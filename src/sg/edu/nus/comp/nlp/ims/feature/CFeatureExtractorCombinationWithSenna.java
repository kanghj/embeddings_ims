/**
 * IMS (It Makes Sense) -- NUS WSD System
 * Copyright (c) 2010 National University of Singapore.
 * All Rights Reserved.
 */
package sg.edu.nus.comp.nlp.ims.feature;

import sg.edu.nus.comp.nlp.ims.feature.embeddings.*;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Feature extractor with combined extractors, including the features using word embeddings
 */
public class CFeatureExtractorCombinationWithSenna extends CFeatureExtractorCombination {
	public CFeatureExtractorCombinationWithSenna() throws IOException {
		this.m_FeatureExtractors.clear();
		this.m_FeatureExtractors.add(new CPOSFeatureExtractor());
		this.m_FeatureExtractors.add(new CCollocationExtractor());
		//this.m_FeatureExtractors.add(new CSurroundingWordExtractor());
		this.m_FeatureExtractors.add(new CSurroundingWordExtractor(1,1));

		//this.m_FeatureExtractors.add(new CSennaContextSumSimilarityFeatureExtractor());

		/*this.m_FeatureExtractors.add(
				new CWordEmbeddingsSentenceSumFeatureExtractor(
						new FrequencyTrickedWordEmbeddings(
								ScaledGloveWordEmbeddings.instance()
						)
				)
		);*/
		//this.m_FeatureExtractors.add(new CWordEmbeddingsSentenceMultFeatureExtractor(SizeScaledSennaWordEmbeddings.instance()));

		this.m_FeatureExtractors.add(new CWordEmbeddingsSentenceSumFeatureExtractor(ScaledWord2VecWordEmbeddings.instance()));

//		this.m_FeatureExtractors.add(new CWordEmbeddingsWindowSumFeatureExtractor(ScaledDependencyWordEmbeddings.instance()));
		//this.m_FeatureExtractors.add(new CWordEmbeddingsSentenceSumFeatureExtractor(ScaledWord2VecWordEmbeddings.instance()));
		//this.m_FeatureExtractors.add(new CWordEmbeddingsSentenceSumFeatureExtractor(DependencyWordEmbeddings.instance(0.05)));

		/*try {
			this.m_FeatureExtractors.add(new CSennaEmbeddingsSyntaxSumFeatureExtractor());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error using CSennaEmbeddingsSyntaxSumFeatureExtractor in Feature Combination", e);
		}*/

		//this.m_FeatureExtractors.add(new CSennaContextSumProductSetFeatureExtractor());
        //this.m_FeatureExtractors.add(new CGloveEmbeddingsContextSumFeatureExtractor());

		// turian concat feature
		//this.m_FeatureExtractors.add(new CSennaContextConcatFeatureExtractor());

		// ims original's collocation feature -> word embeddings style
		//this.m_FeatureExtractors.add(new CSennaCollocationContextConcatFeatureExtractor(ScaledWord2VecWordEmbeddings.instance()));

		//this.m_FeatureExtractors.add(new CSennaCollocationContextSumFeatureExtractor(ScaledWord2VecWordEmbeddings.instance()));



		// idf feature
		//this.m_FeatureExtractors.add(
		//		CWordEmbeddingsWeightedSentenceSumFeatureExtractor.produceWeightedSumExtractorUsing(
		//			new File("senna/hash/token_idf"), new File("senna/hash/words.lst"))
		// );

		try {
			//this.m_FeatureExtractors.add(new CWordEmbeddingsSurroundingWordsFeatureExtractor(DependencyWordEmbeddings.instance()));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error using CWordEmbeddingsSurroundingWordsFeatureExtractor in Feature Combination", e);
		}
	}

	public CFeatureExtractorCombinationWithSenna(
			ArrayList<IFeatureExtractor> p_FeatureExtractors) {
		super(p_FeatureExtractors);
	}
}

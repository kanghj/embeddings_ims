JAVAC = javac
JAVAFLAGS = -O -d bin -encoding utf-8
CLASSPATH = lib/liblinear-1.33-with-deps.jar:lib/jwnl.jar:lib/commons-logging.jar:lib/jdom.jar:lib/trove.jar:lib/maxent-2.4.0.jar:lib/opennlp-tools-1.3.0.jar:lib/weka-3.2.3.jar:lib/libsvm.jar:lib/commons-lang3-3.4/commons-lang3-3.4.jar:lib/stanford-parser-3.6.0-models.jar:lib/stanford-parser.jar:lib/slf4j-api.jar:lib/slf4j-simple.jar

SENSEVAL2TRG = corpora/english-lex-sample/train/eng-lex-sample.training.xml corpora/english-lex-sample/train/eng-lex-sample.training.key
SENSEVAL2TEST = corpora/english-lex-sample/test/eng-lex-samp.evaluation.xml
SENSEVAL2_REORDERED_TRG = reordered-senseval/training_data.xml reordered-senseval/training_data.key
SENSEVAL2_REORDERED_TEST = reordered-senseval/test_data.xml

SENSEVAL3TRG = EnglishLS.train/EnglishLS.train.xml EnglishLS.train/EnglishLS.train.key
SENSEVAL3TEST = EnglishLS.test/EnglishLS.test.xml 

all: build
build:
	mkdir -p bin
	$(JAVAC) -classpath $(CLASSPATH) $(JAVAFLAGS) src/sg/edu/nus/comp/nlp/ims/*/*.java src/sg/edu/nus/comp/nlp/ims/*/*/*.java
	cd bin;	pwd; jar cvf ../ims-`date +%Y-%m-%d`.jar sg; cd ..
	cp ims-`date +%Y-%m-%d`.jar ims.jar
clean:
	@-rm resultDir/*
	@-rm trainedDir/*

prepare_result:
	cd utility; python combined_mul_keys.py;python change_first_token_to_subject.py ; cd ..;

senseval2: clean build
	
	./train_with_senna.bash $(SENSEVAL2TRG)  trainedDir
	./test_with_senna.bash trainedDir/ $(SENSEVAL2TEST) resultDir/ 
	cd utility; python combined_mul_keys.py;python change_first_token_to_subject.py ; cd ..;
	./scorer.bash resultDir/all.combined.amended.result answers+misc/tasks/english-lex-sample/key  

senseval3: clean build
	./train_with_senna.bash $(SENSEVAL3TRG)  trainedDir
	./test_with_senna.bash trainedDir/ $(SENSEVAL3TEST) resultDir/ 
	cd utility; python combined_mul_keys.py;python change_first_token_to_subject.py ; cd ..;
	./scorer.bash resultDir/all.combined.amended.result EnglishLS.test/EnglishLS.test.amended.key 

senseval2_ims: clean build
	
	./train_one.bash $(SENSEVAL2TRG)  trainedDir
	./test_one.bash trainedDir/ $(SENSEVAL2TEST) resultDir/ 
	cd utility; python combined_mul_keys.py;python change_first_token_to_subject.py ; cd ..;
	./scorer.bash resultDir/all.combined.amended.result answers+misc/tasks/english-lex-sample/key  

senseval3_ims: clean build
	./train_one.bash $(SENSEVAL3TRG)  trainedDir
	./test_one.bash trainedDir/ $(SENSEVAL3TEST) resultDir/ 
	cd utility; python combined_mul_keys.py;python change_first_token_to_subject.py ; cd ..;
	./scorer.bash resultDir/all.combined.amended.result EnglishLS.test/EnglishLS.test.amended.key 

senseval2_reordered: clean build
	./train_with_senna.bash $(SENSEVAL2_REORDERED_TRG)  trainedDir
	./test_with_senna.bash trainedDir/ $(SENSEVAL2_REORDERED_TEST) resultDir/ 
	cd utility; python combined_mul_keys.py;python change_first_token_to_subject.py ; cd ..;
	./scorer.bash resultDir/all.combined.amended.result reordered-senseval/test_data.key 

evaluate2_ls:
	cd utility; python combined_mul_keys.py;python change_first_token_to_subject.py ; cd ..;
	
	./scorer.bash resultDir/all.combined.amended.result answers+misc/tasks/english-lex-sample/key
        
evaluate3_ls:
	cd utility; python combined_mul_keys.py;python change_first_token_to_subject.py ; cd ..;
	./scorer.bash resultDir/all.combined.amended.result EnglishLS.test/EnglishLS.test.amended.key 


NOUN_TRG_FILES = one-million-sense-tagged-instances-wn171/noun/ one-million-sense-tagged-instances-wn171/noun/
train_one_million_nouns: build
	./train_with_senna_all_words.bash $(NOUN_TRG_FILES) allWordsTrainedDir

VERB_TRG_FILES = one-million-sense-tagged-instances-wn171/verb/ one-million-sense-tagged-instances-wn171/verb/
train_one_million_verbs: build
	./train_with_senna_all_words.bash $(VERB_TRG_FILES) allWordsTrainedDir

ADJ_TRG_FILES = one-million-sense-tagged-instances-wn171/adj/ one-million-sense-tagged-instances-wn171/adj/
train_one_million_adjs: build
	./train_with_senna_all_words.bash $(ADJ_TRG_FILES) allWordsTrainedDir

ADV_TRG_FILES = one-million-sense-tagged-instances-wn171/adv/ one-million-sense-tagged-instances-wn171/adv/
train_one_million_advs: build
	./train_with_senna_all_words.bash $(ADV_TRG_FILES) allWordsTrainedDir

train_one_million: train_one_million_nouns train_one_million_verbs train_one_million_adjs train_one_million_advs

# https://gitlab.com/kanghongjin/ims_oneMillionTrainedDirContextSumWN21.git 
# https://gitlab.com/kanghongjin/ims_oneMillionTrainedDirWN21.git
# https://gitlab.com/kanghongjin/oneMillionTrainedDirContextSum.git 
# /models/ for the original ims, and for the one million trained sense tagged dataset
#     is provided on the nus nlp website

test_all_words_se2_ims: build
	./testFine.bash models/ corpora/english-all-words/test/eng-all-words.test.xml  examples/se2.eng-all-words.test.lexelt SE2_AW_output examples/wn17.index.sense
	./scorer.bash SE2_AW_output answers+misc/tasks/english-all-words/key


test_all_words_se2_trained: build
	./testFineWithSenna.bash allWordsTrainedDir/ corpora/english-all-words/test/eng-all-words.test.xml examples/se2.eng-all-words.test.lexelt SE2_AW_output examples/wn17.index.sense
	./scorer.bash SE2_AW_output answers+misc/tasks/english-all-words/key

test_all_words_se2: build
	./testFineWithSenna.bash oneMillionTrainedDirContextSum/ corpora/english-all-words/test/eng-all-words.test.xml examples/se2.eng-all-words.test.lexelt SE2_AW_output examples/wn17.index.sense
	./scorer.bash SE2_AW_output answers+misc/tasks/english-all-words/key

test_all_words_se3_ims: build
	./testFine.bash models/ EnglishAW.test/english-all-words.xml  examples/se3.eng-all-words.test.lexelt SE3_AW_output examples/wn17.index.sense	
	./scorer.bash SE3_AW_output EnglishAW.test/EnglishAW.test.key 

test_all_words_se3_trained: build
	./testFineWithSenna.bash allWordsTrainedDir/ EnglishAW.test/english-all-words.xml examples/se3.eng-all-words.test.lexelt SE3_AW_output  examples/wn17.index.sense
	./scorer.bash SE3_AW_output EnglishAW.test/EnglishAW.test.key 

test_all_words_se3: build
	./testFineWithSenna.bash oneMillionTrainedDirContextSum/ EnglishAW.test/english-all-words.xml examples/se3.eng-all-words.test.lexelt SE3_AW_output  examples/wn17.index.sense
	./scorer.bash SE3_AW_output EnglishAW.test/EnglishAW.test.key 

test_fine_all_words_SE2007: build
	./testFineWithSenna.bash oneMillionTrainedDirContextSum/ semeval-2007/test/all-words/english-all-words.test.xml examples/se4.eng-all-words.test.lexelt SE2007_AW_output  examples/wn21.index.sense 
	./scorer.bash SE2007_AW_output  semeval-2007/key/english_all_words_key 

test_fine_all_words_SE2007_trained: build
	./testFineWithSenna.bash allWordsTrainedDirWN21/ semeval-2007/test/all-words/english-all-words.test.xml examples/se4.eng-all-words.test.lexelt SE2007_AW_output  examples/wn21.index.sense 
	./scorer.bash SE2007_AW_output  semeval-2007/key/english_all_words_key 

test_fine_all_words_SE2007_ims: build
	./testFine.bash ims_oneMillionTrainedDirWN21/ semeval-2007/test/all-words/english-all-words.test.xml examples/se4.eng-all-words.test.lexelt SE2007_AW_output  examples/wn21.index.sense 
	./scorer.bash SE2007_AW_output  semeval-2007/key/english_all_words_key 

test_coarse_all_words_SE2007: build
	./testCoarseWithSenna.bash ims_oneMillionTrainedDirContextSumWN21/ semeval-2007-coarse-grained-all-words/test/eng-coarse-all-words.xml SE2007_AW_Coarse_output examples/wn21.index.sense
	cd semeval-2007-coarse-grained-all-words/key; perl scorer.pl ../../SE2007_AW_Coarse_output

test_coarse_all_words_SE2007: build
	./testCoarseWithSenna.bash allWordsTrainedDirWN21/ semeval-2007-coarse-grained-all-words/test/eng-coarse-all-words.xml SE2007_AW_Coarse_output examples/wn21.index.sense
	cd semeval-2007-coarse-grained-all-words/key; perl scorer.pl ../../SE2007_AW_Coarse_output
	
test_coarse_all_words_SE2007_ims: build
	./testCoarse.bash ims_oneMillionTrainedDirWN21/ semeval-2007-coarse-grained-all-words/test/eng-coarse-all-words.xml  SE2007_AW_Coarse_output examples/wn21.index.sense 
	cd semeval-2007-coarse-grained-all-words/key; perl scorer.pl ../../SE2007_AW_Coarse_output

UMCorpus_ContextSum = UMCorpus_ContextSum/
# https://gitlab.com/kanghongjin/UMCorpus_ContextSum
UMCorpus_IMS = UMCorpus_IMS/
# https://gitlab.com/kanghongjin/UMCorpus_IMS
generated_ims_format_training.xml = compiled_data_annotations/generated_ims_format_training.xml
generated_ims_format_training.key = compiled_data_annotations/generated_ims_format_training.key

CLWSD_Context_Sum:
	./test_with_senna.bash $(UMCorpus_ContextSum) $(generated_ims_format_training.xml) resultDir/

CLWSD_IMS:
	./test_with_senna.bash $(UMCorpus_IMS) $(generated_ims_format_training.xml) resultDir/

score_CLWSD:
	./scorer.bash resultDir/all.combined.amended.result $(generated_ims_format_training.key)

test_CLWSD_Context_Sum: clean build CLWSD_Context_Sum prepare_result score_CLWSD

test_CLWSD_IMS: clean build CLWSD_IMS prepare_result score_CLWSD
    

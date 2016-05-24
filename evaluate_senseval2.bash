rm resultDir/*
rm trainedDir/*
make
./train_with_senna.bash corpora/english-lex-sample/train/eng-lex-sample.training.xml corpora/english-lex-sample/train/eng-lex-sample.training.key trainedDir
time ./test_with_senna.bash trainedDir/ corpora/english-lex-sample/test/eng-lex-samp.evaluation.xml resultDir/ &> bothoutanderr.txt 
cd utility/
python combined_mul_keys.py 
python change_first_token_to_subject.py 
cd ..
./scorer.bash resultDir/all.combined.amended.result answers+misc/tasks/english-lex-sample/key

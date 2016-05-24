make
rm resultDir/*
rm trainedDir/*
rm bothoutanderr.txt 
./train_with_senna.bash EnglishLS.train/EnglishLS.train.xml EnglishLS.train/EnglishLS.train.key trainedDir
./test_with_senna.bash trainedDir/ EnglishLS.test/EnglishLS.test.xml resultDir/
cd utility/
python combined_mul_keys.py 
python change_first_token_to_subject.py 
cd ..
./scorer.bash resultDir/all.combined.amended.result EnglishLS.test/EnglishLS.test.amended.key 

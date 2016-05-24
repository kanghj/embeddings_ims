rm resultDir/*
rm trainedDir/*
make
./train_generated_ims_format.bash generated_ims_format_training.xml generated_ims_format_training.key trainedDir 
time ./test_with_senna.bash trainedDir/ generated_ims_format_test.xml resultDir/ 
cd utility/
python combined_mul_keys.py 
python change_first_token_to_subject.py 
cd ..
./scorer.bash resultDir/all.combined.amended.result generated_ims_format_test.key

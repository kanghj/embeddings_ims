rm resultDir/*
rm trainedDir/*
make
./train_generated_ims_format.bash generated_ims_format_training_bilingual.xml generated_ims_format_training_bilingual.key trainedDir 
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/coach.xml resultDir/ 
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/education.xml resultDir/ 
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/execution.xml resultDir/ 
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/figure.xml resultDir/ 
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/job.xml resultDir/ 
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/letter.xml resultDir/ 
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/match.xml resultDir/ 
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/mission.xml resultDir/
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/mood.xml resultDir/
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/paper.xml resultDir/
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/post.xml resultDir/
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/pot.xml resultDir/
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/range.xml resultDir/
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/rest.xml resultDir/
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/ring.xml resultDir/
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/scene.xml resultDir/
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/side.xml resultDir/
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/soil.xml resultDir/
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/strain.xml resultDir/
time ./test_with_senna.bash trainedDir/ cross-lingual/TestSent_Task3/test.xml resultDir/



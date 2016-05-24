

"""
python script to run training script over the onemillion sense-tagged files. Mainly to train models for the all-words task

bash commands that this executes:
train_one        : using normal ims, OR
train_with_senna : using ims+word embedding features as defined in the java file: CFeatureCombinationWithSenna.java
"""

from subprocess import call
import os
from utility import SemevalFilePaths

# assumes that the directories are ordered as ../onemillion/<pos_type>/<target_word>/training.data.[(xml)|(key)]
pos_types_directory = os.listdir('./onemillion')

for type_directory in pos_types_directory:
    target_words = os.listdir('./onemillion/' + type_directory)
    for target_word in target_words:
        xml_file_location = './onemillion/' + type_directory + '/' + target_word + '/training_data.xml'
        key_file_location = './onemillion/' + type_directory + '/' + target_word + '/training_data.key'
        output_directory = 'oneMillionTrainedDir'
        
        SemevalFilePaths.make_dir(output_directory)
        call(["./train_with_senna.bash", xml_file_location, key_file_location, output_directory])
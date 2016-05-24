import csv
from lxml import etree as ET
import codecs
import glob
import os
import logging
import random

import errno

from collections import Counter

from itertools import izip

from InstanceUtils import *
from FilePaths import *

# note for reference: 
#              senseval-2 : corpora/english-lex-sample/train/eng-lex-sample.training.xml
#                         : corpora/english-lex-sample/train/eng-lex-sample.training.key
#              senseval-3 : /home/kanghj/ims_0.9.2/EnglishLS.train/EnglishLS.train.xml
#                           /home/kanghj/ims_0.9.2/EnglishLS.train/EnglishLS.train.key       


#TODO missing test data!!


def sense_ids_num_occurences(instances):
    result = Counter()

    for lexelt, instances_of_lexelt in instances.iteritems():

        for instance in instances_of_lexelt:
            label = instance[2]
            result[(lexelt, label)] += 1

    return result


def all_senseids(all_instances):

    count_of_each_sense = sense_ids_num_occurences(all_instances)
    return count_of_each_sense.keys(), count_of_each_sense


def does_all_senses_have_sufficient_training(all_data, training_data):
    
    all_senses, count_of_each_sense = all_senseids(all_data)
    num_occurences = sense_ids_num_occurences(training_data)

    for lexelt_and_sense in all_senses:
        if num_occurences[lexelt_and_sense] < 2:
            print lexelt_and_sense
            print "orginial count " , count_of_each_sense[lexelt_and_sense]
            return False
    return True

def remove_single_occurence(all_instances):
    num_occurences = sense_ids_num_occurences(all_instances)

    for lexelt_and_sense, count in num_occurences.iteritems():
        if count < 2:
            lexelt, sense = lexelt_and_sense

            all_instances[lexelt] = [x for x in all_instances[lexelt] if x[2] != sense]

    return all_instances



_config = {
        'xml_file': SENSEVAL_2_XMl,
        'key_file': SENSEVAL_2_KEY,
        'test_xml_file': SENSEVAL_2_TEST_XMl,
        'test_key_file': SENSEVAL_2_TEST_KEY, 
        'dtd' : '/home/kanghj/ims_0.9.2/corpora/english-lex-sample/train/lexical-sample.dtd',
        'output_directory': 'reordered-senseval/',
        }

if __name__ == '__main__':

    test_instances, _      = all_senseids(instances_from([ (SENSEVAL_2_TEST_XMl, _config['dtd'], SENSEVAL_2_TEST_KEY) ]))
    training_instances, _  = all_senseids(instances_from([ (SENSEVAL_2_XMl, _config['dtd'], SENSEVAL_2_KEY) ]))
    test_instances = set(test_instances)
    training_instances = set(training_instances)
    print(test_instances.difference(training_instances))
    print('===')


    output_directory = _config['output_directory']
    try:
        os.makedirs(output_directory)
    except OSError as exc: 
        if exc.errno == errno.EEXIST and os.path.isdir(output_directory):
            pass
        else: raise
    
    
    all_instances = instances_from([(SENSEVAL_2_XMl, _config['dtd'], SENSEVAL_2_KEY), 
                                   (SENSEVAL_2_TEST_XMl, _config['dtd'], SENSEVAL_2_TEST_KEY)])

    all_instances  = remove_single_occurence(all_instances)
    num_occurences = sense_ids_num_occurences(all_instances)

    seed = 123
    random.seed(seed)

    is_senses_have_training_data = False
    counter = 0
    while not is_senses_have_training_data:
        training_data = {}
        test_data = {}
        
        for lexelt_id, instances in all_instances.iteritems():
            for instance in instances:
                is_test = random.randint(1, 4) == 4 
                data_to_add_to = test_data if is_test else training_data
                
                try:
                    data_to_add_to[lexelt_id].append(instance)
                except KeyError as e:
                    data_to_add_to[lexelt_id] = []
                    data_to_add_to[lexelt_id].append(instance)

        is_senses_have_training_data = does_all_senses_have_sufficient_training(all_instances, training_data)
        if not is_senses_have_training_data:
            counter += 1
            print counter


    test_xml, test_keys = create_documents(test_data)
    training_xml, training_keys = create_documents(training_data)

    test_xml.write(output_directory + 'test_data.xml', pretty_print=True)
    write_labels_file(output_directory + 'test_data.key', test_keys)
    training_xml.write(output_directory + 'training_data.xml', pretty_print=True)
    write_labels_file(output_directory + 'training_data.key', training_keys)
            
    print 'reorder standard dataset\'s data completed for '
    print _config
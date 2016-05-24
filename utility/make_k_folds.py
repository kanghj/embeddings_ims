import csv
from lxml import etree as ET
import codecs
import glob
import os
import logging

import random

import argparse

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

"""
to amke k-fold cross validation dataset

"""

_config = {
        'xml_file': SENSEVAL_2_XMl,
        'key_file': SENSEVAL_2_KEY,
        'test_xml_file': SENSEVAL_2_TEST_XMl,
        'test_key_file': SENSEVAL_2_TEST_KEY, 
        'dtd' : '/home/kanghj/ims_0.9.2/corpora/english-lex-sample/train/lexical-sample.dtd',
        'output_directory': 'reordered-senseval/',
        'test_index': 0,
        'num_folds': 5,
        }

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("testindex",  nargs='?')
    args =  parser.parse_args()

    
    test_index = _config['test_index'] if args.testindex is None else int(args.testindex)




    output_directory = _config['output_directory']
    try:
        os.makedirs(output_directory)
    except OSError as exc: 
        if exc.errno == errno.EEXIST and os.path.isdir(output_directory):
            pass
        else: raise
    
    
    all_instances = instances_from([(_config['xml_file'], _config['dtd'], _config['key_file']), 
                                    (_config['test_xml_file'], _config['dtd'], _config['test_key_file'])])

    # let's do k-fold cross-validation
    #     find the limits of each task for supervised training
    #     

    training_data = {}
    test_data = {}
    for lexelt_id, instances in all_instances.iteritems():
        for i, instance in enumerate(instances):
            is_test = i % _config['num_folds'] == test_index
            data_to_add_to = test_data if is_test else training_data
            
            try:
                data_to_add_to[lexelt_id].append(instance)
            except KeyError as e:
                data_to_add_to[lexelt_id] = []
                data_to_add_to[lexelt_id].append(instance)

   
    test_xml, test_keys = create_documents(test_data)
    training_xml, training_keys = create_documents(training_data)

    test_xml.write( output_directory     + 'test_data.xml', pretty_print=True)
    write_labels_file( output_directory  + 'test_data.key', test_keys)
    training_xml.write( output_directory + 'training_data.xml', pretty_print=True)
    write_labels_file( output_directory  + 'training_data.key', training_keys)
            
    print 'reorder standard dataset\'s data completed for '
    print _config
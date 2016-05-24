import csv
from lxml import etree as ET
import codecs
import glob
import os
import logging
import string

import errno

from nltk.stem import WordNetLemmatizer

from collections import Counter
import FilePaths

from itertools import izip

"""


"""

MIN_OCCURENCE_TO_ADD = 1
wnl = WordNetLemmatizer()

def context_common_words(xml_and_dtd, key_file):
    """

    """
    xml_file = xml_and_dtd[0]
    dtd = ET.DTD(xml_and_dtd[1])    # TODO no longer needed, remove
    parser = ET.XMLParser(dtd_validation=True)
    print xml_and_dtd

    tree = ET.parse(xml_file, parser)

    xml_instances = tree.getroot().findall('.//instance')

    ids     = map(lambda x: x.attrib['id'].strip(), xml_instances)

    heads   = map(lambda x: ' '.join(x.find('context').text.split()[:]) or '', xml_instances)
    # the part of the context behind the <head> doesn't get included in head
    # so we use the tail of the head to obtain it
    #tails   = map(lambda x: ' '.join(x.find('.//head').tail.split()[:]) or '', xml_instances)
    tails   = map(lambda x: x.find('.//head') or '', xml_instances)

    full_contexts = [(head + tail).split() for head, tail in izip(heads, tails)]

    with open(key_file) as labels_file:
        # TODO! check order, or do this by non-ordered method
        labels = [line.split(' ')[2].strip() for line in labels_file]

    word_to_common_contexual_words = {}
    senseid_num_examples = Counter(labels)

    for instance_id, context, label in izip(ids, full_contexts, labels):
        instance_id = instance_id.strip()
      
        target_word = instance_id.split('.')[0]
        
        for word in context:
            #word = word.strip(string.punctuation).lower()
            word = wnl.lemmatize(word).lower().rstrip(string.punctuation)
            print word

            try:
                word_to_common_contexual_words[target_word][word] += 1
            except KeyError as e:
                word_to_common_contexual_words[target_word] = Counter()
                word_to_common_contexual_words[target_word][word] += 1

    for word, count in word_to_common_contexual_words.iteritems():
        if count < MIN_OCCURENCE_TO_ADD: 
            del word_to_common_contexual_words[word]

    return word_to_common_contexual_words



# note for reference: 
#              senseval-2 : corpora/english-lex-sample/train/eng-lex-sample.training.xml
#                         : corpora/english-lex-sample/train/eng-lex-sample.training.key
#               senseval-3 : /home/kanghj/ims_0.9.2/EnglishLS.train/EnglishLS.train.xml
#                           /home/kanghj/ims_0.9.2/EnglishLS.train/EnglishLS.train.key       


# todo : add directory mode for xml file and key file
_config = {
        'xml_file': FilePaths.SENSEVAL_3_XML,
        'key_file': FilePaths.SENSEVAL_3_KEY,
        'dtd' : '/home/kanghj/ims_0.9.2/corpora/english-lex-sample/train/lexical-sample.dtd',
        'feature_extractor': context_common_words,
        'output_directory': 'common_words/'

        }

if __name__ == '__main__':
    FilePaths.make_dir(_config['output_directory'] )
    
    
    word_to_common_contexual_words = _config['feature_extractor']((_config['xml_file'], _config['dtd']), _config['key_file'])

    # make a file for each word
    for word, context_common_words in word_to_common_contexual_words.iteritems():
        with codecs.open(_config['output_directory'] + word, 'w+') as feature_file:

            for context_word in context_common_words:
                try:
                    feature_file.write(context_word.decode('utf8') + '\n')
                except UnicodeEncodeError as e:
                    pass # ignore because it probably doesn't matter
    
    print 'All completed for '
    print _config

import os
import errno

from collections import defaultdict

from lxml import etree as ET
from itertools import izip

import nltk

from FilePaths import *
import re

from nltk.stem.wordnet import WordNetLemmatizer
from nltk.corpus import wordnet


lmtzr = WordNetLemmatizer()

from IPython import embed

_config = {
        'xml_file': '/home/kanghj/ims_0.9.2/semeval-2007-coarse-grained-all-words/test/eng-coarse-all-words.xml',
        'key_file': '/home/kanghj/ims_0.9.2/semeval-2007-coarse-grained-all-words/key/dataset21.test.key',
        'dtd' : '/home/kanghj/ims_0.9.2/corpora/english-lex-sample/train/lexical-sample.dtd',
        'output_directory': 'split-senseval2007-coarse-AW-test/',
        }

_instance_count = 0

def texts_of_xml(xml_file):
    
    parser = ET.XMLParser(dtd_validation=False)  # True for senseval-2, False for senseval-3, todo clean this up...
    tree = ET.parse(xml_file, parser)

    return tree.getroot().findall('.//text')


def get_instances(text):
    result = []
    
    sentences_text = {}

    sentences = text.findall('.//sentence')
    instance_positions = {}

    
    for sentence in sentences:
        sentence_id = sentence.attrib['id']

        sentence_text = sentence.text.replace('\n', ' ')

        instances = sentence.findall('.//instance')

        for instance in instances:
            instance_positions[instance.attrib['id']] = len(sentence_text)

            sentence_text += instance.text.replace('\n', ' ')
            sentence_text += instance.tail.replace('\n', ' ')

        sentences_text[sentence_id] = sentence_text


    instances = text.findall('.//instance')
    for instance in instances:
        instance_id = instance.attrib['id']

        lemma = instance.attrib['lemma'].lower()
        pos = instance.attrib['pos']
        lemma_with_pos = lemma + '.' + pos

        docid = '.'.join(instance_id.split('.')[0])

        sentence_id_of_instance = '.'.join(instance_id.split('.')[:-1])

        sentence_text = sentences_text[sentence_id_of_instance]
        instance_position = instance_positions[instance_id]

        sentence_text_with_head = sentence_text[:instance_position] + \
                                    sentence_text[instance_position:].replace(instance.text, '__head__' + lemma + '__endhead__', 1)

        context_with_head = ''
        prev_sentence_id = int(sentence_id_of_instance[-3:]) - 1
        if prev_sentence_id != 0:
            prev_sentence = sentences_text[sentence_id_of_instance[:-3] + ('%03d' % prev_sentence_id)]
            context_with_head += prev_sentence 

        context_with_head += sentence_text_with_head 

        next_sentence_id = int(sentence_id_of_instance[-3:]) + 1
        print len(sentences_text), next_sentence_id
        if next_sentence_id < len(sentences_text):
            next_sentence = sentences_text[sentence_id_of_instance[:-3] + ('%03d' % next_sentence_id)]
            context_with_head += next_sentence 


        result.append( ( lemma_with_pos, instance_id, context_with_head ) )

    return result


def answers_from_key_file(key_file):
    result = {}
    with open(key_file, 'r') as opened_key_file:
        for line in opened_key_file:
            instance_id = line.split()[1]
            sense_id = line.split()[2]
            result[instance_id] = sense_id

    return result


def make_corpus(xml_file, accepted_instances):
    corpus = ET.Element("corpus")

    texts = texts_of_xml(xml_file)


    ## get all instances
    instances = []
    for text in texts:
        instances.extend( get_instances(text) )
    ## organise instances into map of {lexelt -> [ instances ] }
    lexelt_instances = defaultdict(list)
    for lexelt, instance_id, context_with_head in instances:
        if instance_id not in accepted_instances:
            continue
        lexelt_instances[lexelt].append( (lexelt, instance_id, context_with_head) )



    lexelts_words = lexelt_instances.keys()
    for lexelt_word in lexelts_words:
        lexelt_node = ET.SubElement(corpus, "lexelt", item=lexelt_word)


        instances = lexelt_instances[lexelt_word]
        for lexelt, instance_id, context_with_head in instances:
            instance_node = ET.SubElement(lexelt_node, "instance", id=instance_id, docsrc="dummy_docsrc")
            context = ET.SubElement(instance_node, "context")
            context.text = context_with_head



    return corpus



if __name__ =="__main__":
    output_directory = _config['output_directory']
    make_dir(output_directory)

    
    answers = answers_from_key_file(_config['key_file'])
    corpus = make_corpus(_config['xml_file'], answers.keys())

    instance_ids_order = map(lambda instance: instance.attrib['id'], corpus.findall('.//instance'))
    
    
    with open(output_directory + 'output.xml', 'w') as outfile,\
         open(output_directory + 'output.key', 'w') as outfile_key:
        outfile.write(ET.tostring(corpus, pretty_print= True).replace("__head__", "<head>").replace("__endhead__", "</head>"))

        for instance_id in instance_ids_order:
            _instance_count  += 1
            outfile_key.write('dummy_docsrc ' + instance_id + " " + answers[instance_id])
            outfile_key.write('\n')


        print "instance counts ... ", _instance_count
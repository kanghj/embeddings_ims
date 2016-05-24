import os
import errno

from collections import defaultdict

from lxml import etree as ET
from itertools import izip

import nltk

from FilePaths import *
import string
import re

from nltk.stem.wordnet import WordNetLemmatizer
from nltk.corpus import wordnet


lmtzr = WordNetLemmatizer()

from IPython import embed

_config = {
        'xml_file': SENSEVAL_2_AW_XML,
        'key_file': SENSEVAL_2_AW_KEY,
        'dtd' : '/home/kanghj/ims_0.9.2/corpora/english-lex-sample/train/lexical-sample.dtd',
        'output_directory': 'split-senseval2-AW-test/',
        'id_to_lemma_map_path' : '/home/kanghj/ims_0.9.2/examples/se2.eng-all-words.test.lexelt',
        }

def texts_of_xml(xml_file):
    
    parser = ET.XMLParser(dtd_validation=False)  # True for senseval-2, False for senseval-3, todo clean this up...
    tree = ET.parse(xml_file, parser)

    return tree.getroot().findall('.//text')

def get_wordnet_pos(treebank_tag):

    if treebank_tag.startswith('J'):
        return wordnet.ADJ
    elif treebank_tag.startswith('V'):
        return wordnet.VERB
    elif treebank_tag.startswith('N'):
        return wordnet.NOUN
    elif treebank_tag.startswith('R'):
        return wordnet.ADV
    else:
        ## lolololol
        return wordnet.VERB


_map_of_id_to_lemma = {}
def lemma_of_id(instance_id):
    if not _map_of_id_to_lemma:
        with open(_config['id_to_lemma_map_path']) as id_to_lemma_file:
            for line in id_to_lemma_file:
                instance_id = line.split()[0]
                lemma = line.split()[1]
            
                _map_of_id_to_lemma[instance_id] = lemma
    return _map_of_id_to_lemma[instance_id]



def change_heads_to_instances(text):
    heads = text.findall('.//head')

    tokens = []
    tokens.extend(text.text.split())

    for head in heads:
        head_text = head.text
        if head_text == '%':
            head_text = 'percent'
        tokens.append('be' if mysterious_be_match(head_text) else head_text )
        following_text = head.tail
        following_text.replace('%', 'percent')
        tokens.extend( ( 'be' if mysterious_be_match(token) else token for token in following_text.split() ) )

    tokens_with_pos = nltk.pos_tag(tokens)

    context = ' '.join(tokens)

    results = []
    current_index_in_context = len(text.text) - 1
    current_index_in_tokens_with_pos = 0
    for head in heads:
        head_id = head.attrib['id'] # to become instance id
        instance_id = head_id

        replaced_head_text = 'be' if mysterious_be_match(head.text) else head.text
        if replaced_head_text == '%':
            replaced_head_text = 'percent'

        head_pos_index = -1
        for i in xrange(current_index_in_tokens_with_pos, len(tokens_with_pos)):
            token_and_pos = tokens_with_pos[i]
            if token_and_pos[0] == replaced_head_text:
                head_pos_index = i
                break

        if head_pos_index == -1:
            raise Exception("can't find token")

        print head_id
        print tokens_with_pos[ head_pos_index ]
        #head_pos = get_wordnet_pos(tokens_with_pos[ head_pos_index ][1] )
        print lemma_of_id(instance_id)
        try:
            head_pos = lemma_of_id(instance_id).split('.')[1]
        except:
            head_pos = 'U'

        print replaced_head_text
        print head_pos

        #lemmatised_head = lmtzr.lemmatize(replaced_head_text, head_pos )
        lemmatised_head = lemma_of_id(instance_id)
        
        print current_index_in_context
        print lemmatised_head
        #print context[:current_index_in_context] 

        if not replaced_head_text.endswith('.') and not replaced_head_text.endswith(','):
            context_with_head = context[:current_index_in_context] \
                              + re.sub('\\b' + re.escape(replaced_head_text) + '\\b', "__head__" + replaced_head_text.lower() + "__endhead__", context[current_index_in_context:], count=1)
        else:
            context_with_head = context[:current_index_in_context] \
                              + re.sub('\\b' + re.escape(replaced_head_text) , "__head__" + replaced_head_text.lower() + "__endhead__", context[current_index_in_context:], count=1)

        if '__head__' not in context_with_head: 
            print context[:current_index_in_context]
            print ".............................................................."
            print context[current_index_in_context:]
            raise ValueError

        instance = (lemmatised_head.lower(), instance_id, context_with_head)
        results.append(instance)

        current_index_in_context = context.index(replaced_head_text, current_index_in_context)


    return results

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
        instances.extend( change_heads_to_instances(text) )
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

BE_MATCHER = re.compile("'([sm]|re)")
def mysterious_be_match(token):
    if BE_MATCHER.match(token):
        return True
    else:
        return False

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
            outfile_key.write('dummy_docsrc ' + instance_id + " " + answers[instance_id])
            outfile_key.write('\n')

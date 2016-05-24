import csv
from lxml import etree as ET
import codecs
import glob
import os
import logging
import random
import re

import errno

from collections import Counter
from itertools import izip


import nltk

"""


"""



def lexelt_instances_and_labels(xml_and_dtd, key_file, filter_condition = None):
    """
    returns [(lexelt_id, instance, label) , ... ], a list containing tuples of (lexelt_id, instance, label)
    """
    def default_filter(instance):
        return True
    
    filter_condition = default_filter if filter_condition is None else filter_condition

    xml_file = xml_and_dtd[0]
    dtd = ET.DTD(xml_and_dtd[1])    # TODO no longer needed, can remove

    parser = ET.XMLParser(dtd_validation=False)  # True for senseval-2, False for senseval-3, todo clean this up...

    tree = ET.parse(xml_file, parser)

    #if dtd.validate(tree) is False:
    #    raise ValueError("dtd validation failed")

    xml_instances = []
    lexelts = tree.getroot().findall('.//lexelt')
    
    for lexelt in lexelts:
        lexelt_id = lexelt.attrib['item'].strip()
        lexelt_instances = lexelt.findall('.//instance')
        lexelt_and_instances = ((lexelt_id, instance) for instance in lexelt_instances)
        xml_instances.extend(lexelt_and_instances)

    print len(xml_instances)

    with open(key_file) as labels_file:
        # TODO! check order, or do this by non-ordered method
        labels = [line.split(' ')[2].strip() for line in labels_file]

    instances_of_lexelt = {}

    for (lexelt_id, instance), label in izip(xml_instances, labels):
        if not filter_condition(instance):
            continue

        try:
            instances_of_lexelt[lexelt_id].append((lexelt_id, instance, label))
        except KeyError as e:
            instances_of_lexelt[lexelt_id] = []
            instances_of_lexelt[lexelt_id].append((lexelt_id, instance, label))

    # get lexelts. e.g. art.001 : art
    return instances_of_lexelt



def instances_from(xml_and_dtd_and_key_files):

    if xml_and_dtd_and_key_files is None:
        raise ValueError('Nones are not allowed')

    if isinstance(xml_and_dtd_and_key_files[0], str):
        raise ValueError('pass in an iterator containing tuples of values! e.g. [("xml_file_path", "dtd_file_path" , "key_file_path"), ..]')

    try:
        collected_instances = {}
        
        for xml_file, dtd_file, key_file in xml_and_dtd_and_key_files:
            xml_instances = lexelt_instances_and_labels((xml_file, dtd_file), key_file)
            collected_instances.update(xml_instances)

    except Exception as e:
        logging.exception(e)
        raise e

    return collected_instances


def get_labels_from_xml(single_xml_file):
    """
    Intending to use this for semeval 2007 to extract labels from xml file to make the separate key file
    """
    parser = ET.XMLParser(dtd_validation=False)  # True for senseval-2, False for senseval-3, todo clean this up...

    tree = ET.parse(single_xml_file, parser)

    #if dtd.validate(tree) is False:
    #    raise ValueError("dtd validation failed")

    xml_instances = []
    instances = tree.getroot().findall('.//instance')

    result = []

    for instance in instances:
        instance_id = instance.attrib['id']
        
        label = instance.findall('.//answer')[0].attrib['senseid']
        
        lexelt = label.split('%')[0]

        result.append((lexelt, instance_id, label))

    return result
    

def create_documents(lexelt_to_instances_dict):

    root = ET.Element("corpus", lang="english")
    labels = []
    for lexelt_id, instances in lexelt_to_instances_dict.iteritems():
        print lexelt_id
        lexelt_node = ET.SubElement(root, "lexelt", item=lexelt_id)
        for lex_id, instance_element, label in instances:
            assert lex_id == lexelt_id # todo, remove due to duplicate info

            lexelt_node.append(instance_element)

            instance_id = instance_element.attrib['id'].strip()
            labels.append((instance_id.split('.')[0], instance_id, label))

    return (ET.ElementTree(root) , labels)


def write_labels_file(file_path, columns_as_tuples):
    with open(file_path, 'w+') as outfile:
        for row in columns_as_tuples:
            for col in row:
                print col
                outfile.write(col + " ")
            outfile.write('\n')
import os
import errno

from lxml import etree as ET
from itertools import izip
from InstanceUtils import *
from FilePaths import *

from IPython import embed
"""
from a dataset with split data, such as the one-million-sense-tagged-instances-wn30
join the sense tags into specified files

"""

_config = {
        'input_directories': [#'/media/kanghj/4AA0EB67A0EB5849/Users/user/Downloads/one-million-sense-tagged-instances-wn171/adj/',
                                #'/media/kanghj/4AA0EB67A0EB5849/Users/user/Downloads/one-million-sense-tagged-instances-wn171/adv/',
                                #'/media/kanghj/4AA0EB67A0EB5849/Users/user/Downloads/one-million-sense-tagged-instances-wn171/noun/',
                                #'/media/kanghj/4AA0EB67A0EB5849/Users/user/Downloads/one-million-sense-tagged-instances-wn171/verb/'
                                #'/home/kanghj/ims_0.9.2/utility/split-senseval3-AW-test/'
                                #'/media/kanghj/4AA0EB67A0EB5849/Users/user/Downloads/one-million-sense-tagged-instances-wn21/adj/',
                                #'/media/kanghj/4AA0EB67A0EB5849/Users/user/Downloads/one-million-sense-tagged-instances-wn21/adv/',
                                #'/media/kanghj/4AA0EB67A0EB5849/Users/user/Downloads/one-million-sense-tagged-instances-wn21/noun/',
                                '/media/kanghj/4AA0EB67A0EB5849/Users/user/Downloads/one-million-sense-tagged-instances-wn21/verb/'
                            ],
        'dtd' : '/home/kanghj/ims_0.9.2/corpora/english-lex-sample/train/lexical-sample.dtd',
        'output_directory': 'onemillion/verb/',
        }


def make_dir(output_directory):
    try:
        os.makedirs(output_directory)
    except OSError as exc: 
        if exc.errno == errno.EEXIST and os.path.isdir(output_directory):
            pass
        else: raise


if __name__ == '__main__':
    output_directory = _config['output_directory']
    make_dir(output_directory)
    
    input_triples = []
    for directory in _config['input_directories']:
        files_in_dir = os.listdir(directory)
        target_words = set([x.split('.xml')[0].split('.key')[0] for x in files_in_dir])

        for word in target_words:
            triple = ( directory + word + '.xml', _config['dtd'], directory + word + '.key' )
            input_triples.append(triple)

    instances = instances_from(input_triples)

    for lexelt_id, instances in instances.iteritems():

        output_directory_for_lexelt = output_directory + lexelt_id + '/'
        make_dir(output_directory_for_lexelt)

        training_data = {}
        training_data[lexelt_id] = instances

        training_xml, training_keys = create_documents(training_data)
        training_xml.write(output_directory_for_lexelt + 'training_data.xml', pretty_print=True)
        write_labels_file(output_directory_for_lexelt + 'training_data.key', training_keys)

    """
    training_xml, training_keys = create_documents(instances)
    training_xml.write(output_directory + 'training_data.xml', pretty_print=True)
    write_labels_file(output_directory + 'training_data.key', training_keys)"""


    print 'All completed for '
    print _config
    print "num instances", len(instances)
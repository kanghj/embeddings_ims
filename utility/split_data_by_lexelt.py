import os
import errno

from lxml import etree as ET
from itertools import izip
from InstanceUtils import *
from FilePaths import *

# note for reference: 
#              senseval-2 : corpora/english-lex-sample/train/eng-lex-sample.training.xml
#                         : corpora/english-lex-sample/train/eng-lex-sample.training.key
#              senseval-3 : /home/kanghj/ims_0.9.2/EnglishLS.train/EnglishLS.train.xml
#                           /home/kanghj/ims_0.9.2/EnglishLS.train/EnglishLS.train.key       

_config = {
        'xml_file': '/home/kanghj/ims_0.9.2/utility/split-senseval2-AW-test/output.xml',
        'key_file': '/home/kanghj/ims_0.9.2/utility/split-senseval2-AW-test/output.key',
        'dtd' : '/home/kanghj/ims_0.9.2/corpora/english-lex-sample/train/lexical-sample.dtd',
        'output_directory': 'split-senseval2-AW-test/',
        }



if __name__ == '__main__':
    output_directory = _config['output_directory']
    make_dir(output_directory)
    
    instances = instances_from([(_config['xml_file'], _config['dtd'], _config['key_file'])])
    
    num_inst = 0
    for lexelt_id, instances in instances.iteritems():
        pos_type = lexelt_id.split('.')[-1]

        output_directory_for_lexelt = output_directory + pos_type + '/' + lexelt_id + '/'
        make_dir(output_directory_for_lexelt)

        training_data = {}
        training_data[lexelt_id] = instances

        training_xml, training_keys = create_documents(training_data)

        training_xml.write(output_directory_for_lexelt + 'training_data.xml', pretty_print=True)
        write_labels_file(output_directory_for_lexelt + 'training_data.key', training_keys)        

        num_inst += len(instances)
    
    print 'All completed for '
    print _config
    print "num instances", num_inst
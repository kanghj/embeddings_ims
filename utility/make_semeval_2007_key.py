import os
import errno

from lxml import etree as ET
from itertools import izip
from InstanceUtils import *
from FilePaths import *

"""
Make a key file for semeval-2007 LS. the data downloaded doesn't contain the files
"""

_config = {
        'xml_file': '/home/kanghj/ims_0.9.2/semeval-2007/train/lexical-sample/english-lexical-sample.train.xml',
        # no key file
        'dtd' : '/home/kanghj/ims_0.9.2/corpora/english-lex-sample/train/lexical-sample.dtd',
        'output_file': '/home/kanghj/ims_0.9.2/semeval-2007/train/lexical-sample/english-lexical-sample.train.key',
        }

if __name__ == '__main__':

	labels = get_labels_from_xml(_config['xml_file'])

	write_labels_file(_config['output_file'], labels)   
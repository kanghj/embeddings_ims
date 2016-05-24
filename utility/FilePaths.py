
SENSEVAL_2_XMl = '/home/kanghj/ims_0.9.2/corpora/english-lex-sample/train/eng-lex-sample.training.xml'
SENSEVAL_2_KEY = '/home/kanghj/ims_0.9.2/corpora/english-lex-sample/train/eng-lex-sample.training.key'
SENSEVAL_2_TEST_XMl = '/home/kanghj/ims_0.9.2/corpora/english-lex-sample/test/eng-lex-samp.evaluation.xml'
SENSEVAL_2_TEST_KEY = '/home/kanghj/ims_0.9.2/answers+misc/tasks/english-lex-sample/key'

SENSEVAL_3_XML = '/home/kanghj/ims_0.9.2/EnglishLS.train/EnglishLS.train.xml'
SENSEVAL_3_KEY = '/home/kanghj/ims_0.9.2/EnglishLS.train/EnglishLS.train.key'
SENSEVAL_3_TEST_XML = '/home/kanghj/ims_0.9.2/EnglishLS.test/EnglishLS.test.xml'
SENSEVAL_3_TEST_KEY = '/home/kanghj/ims_0.9.2/EnglishLS.test/EnglishLS.test.amended.key'

SENSEVAL_3_AW_XML = "/home/kanghj/ims_0.9.2/EnglishAW.test/english-all-words.xml"
SENSEVAL_3_AW_KEY =  "/home/kanghj/ims_0.9.2/EnglishAW.test/EnglishAW.test.key"

SENSEVAL_2_AW_XML = "/home/kanghj/ims_0.9.2/corpora/english-all-words/test/eng-all-words.test.xml"
SENSEVAL_2_AW_KEY =  "/home/kanghj/ims_0.9.2/answers+misc/tasks/english-all-words/key"

def make_dir(output_directory):
    """
    Utility function to make 
    """
    import os
    import errno
    try:
        os.makedirs(output_directory)
    except OSError as exc: 
        if exc.errno == errno.EEXIST and os.path.isdir(output_directory):
            pass
        else: raise

import re
from collections import Counter

id_to_lemma = {}
lemma_total_count = Counter()
lemma_wrong_answer_count = Counter()


system_wrong_answer_file = './semeval2007-coarse-wrong-answers.txt'
baseline_wrong_answer_file = './semeval-2007-coarse-grained-all-words/key/fs_baseline_wrong_answers.txt'
rnn_wrong_answers = './rnn-semeval-2007-wrong-answers.txt'

with open('./semeval-2007-coarse-grained-all-words/key/dataset21.test.key', 'r') as key_file,\
     open(rnn_wrong_answers, 'r') as wrong_answers_file:
    for line in key_file:
        lemma = line.split('lemma=')[1].strip()
        instance_id = line.split(' ')[1].strip()


        id_to_lemma[instance_id] = lemma
        lemma_total_count[lemma] += 1

    print id_to_lemma

    for line in wrong_answers_file:
        instance_id = line.split()[0]
        lemma = id_to_lemma[instance_id]
        lemma_wrong_answer_count[lemma] += 1

    sorted_lemmas = sorted(lemma_total_count.keys())
    for lemma in sorted_lemmas:
        print lemma + ", " + str( float(lemma_wrong_answer_count[lemma]) / lemma_total_count[lemma] )



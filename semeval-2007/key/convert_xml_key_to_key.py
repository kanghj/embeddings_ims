import re


def convert(xml_line):
    """
    Heavily coupled with the input. Assumes that each line is an xml-formated tuple (<answer head =".." senseid="..")
    """
    head = re.search("head\\s*=\\s*\"(.*?)\"", xml_line).group(1)
    senseid = re.search("senseid\\s*=\\s*\"(.*?)\"", xml_line).group(1)
    print head ,";",senseid

    return (head, senseid)


if __name__ == "__main__":
    with open('english-all-words.test.key') as key_file,\
         open('english_all_words_key', 'w+') as out_key:
        for line in key_file:
            head, senseid = convert(line)

            out_key.write(head.split('.')[0] + " " + head + " " +  senseid)
            out_key.write('\n')




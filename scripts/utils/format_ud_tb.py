__author__="Federico Fancellu"

from argparse import ArgumentParser
import codecs

class TreeModifer(object):
    def __init__(self,args):
        self.inputTB = codecs.open(args.t,'rb','utf8')
        self.outputTB = codecs.open(args.o,'wb','utf8')
        if args.a=='toUniversalPOS':
            self.all_universal_POS()
        self.close_streams()

    def all_universal_POS(self):
        for line in self.inputTB:
            if line[0].isdigit():
                items = line.strip().split('\t')
                items[4] = items[3]
                self.outputTB.write('\t'.join(items) + '\n')
            else:
                self.outputTB.write(line)

    def close_streams(self):
        self.inputTB.close()
        self.outputTB.close()


if __name__=="__main__":
    parser = ArgumentParser()
    parser.add_argument('-t',help="Filepath to the UD treebank",required=True)
    parser.add_argument('-o',help="Filepath to output UD treebank",required=True)
    parser.add_argument('-a',choices=['toStandardPOS','toUniversalPOS'],required=True)
    args = parser.parse_args()
    TreeModifer(args)

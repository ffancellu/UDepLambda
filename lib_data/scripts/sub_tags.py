import sys
import codecs

def main(ud, tags,_out):
    with codecs.open(ud,'rb','utf8') as ud_,\
        codecs.open(tags,'rb','utf8') as tags_,\
        codecs.open(_out,'wb','utf8') as _out_:
        
        for line in tags_:
            to_dump = list()
            wordsTags = map(lambda x: x.split('_'),line.strip().split())
            for wt in wordsTags:
                w,t = wt[0],wt[-1]
                next_ud = next(ud_).strip()
                if not next_ud:
                    _out_.write('\n')
                    next_ud = next(ud_).strip()
                tokens = next_ud.split('\t')
                tokens[3] = t
                _out_.write('\t'.join(tokens)+'\n')


if __name__=="__main__":
    ud = sys.argv[1]
    tags = sys.argv[2]
    _out = sys.argv[3]
    main(ud, tags, _out)


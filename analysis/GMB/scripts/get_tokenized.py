__author__="Federico Fancellu"

import os
import sys
import codecs

def traverse_folder(root_folder):
    for _dir in os.listdir(root_folder):
        child_path = os.path.join(root_folder,_dir)
        if os.path.isdir(child_path):
            traverse_folder(child_path)
        elif child_path.endswith('tok.off'):
	    create_tok(child_path)
            sys.exit(0)
            

def create_tok(fname):
    words = []
    current_id = 1 
    with codecs.open(fname,'rb','utf8') as tok_file:
        for line in tok_file:
            _,_,_id,word = line.strip().split()
            if int(_id[0])!=current_id:
                words.append('\n')
                current_id = int(_id[0]) 
            words.append(word)
    with codecs.open(os.path.join(os.path.dirname(fname),'en.tok.par'),'wb','utf8') as tok_out:
        print os.path.join(os.path.dirname(fname),'en.tok.par')
        lines = [x.strip() for x in ' '.join(words).split('\n')]
        for line in lines:
            tok_out.write(line+'\n')

if __name__=="__main__":
    traverse_folder(sys.argv[1])
          

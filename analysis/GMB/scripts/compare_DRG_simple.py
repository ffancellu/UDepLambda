from __future__ import division

__author__="Federico Fancellu"

import os
import sys
import codecs
import re
import itertools
from collections import Counter
from operator import itemgetter


MODE = 'relaxed'

def get_triplets(text):
    triplets = set()
    for line in text:
        line = line.strip()
        if line and not line.startswith('%'):
            #(s)tart node, (r)elation, (e)nd node
            s,r,e = line.split()[:3]
            if r not in ['main','referent']:
                if MODE=='relaxed':
                    s = re.sub(r'[0-9]','',s)
                    s = re.sub(r'[\(|\)]','',s)
                    e = re.sub(r'[0-9]','',e)
                    e = re.sub(r'[\(|\)]','',e)
                    triplets.add((s,r,e))
                elif MODE=='hard':
                    triplets.add((s,r,e))
    return triplets

def traverse_folder(root_folder):
    for _dir in os.listdir(root_folder):
        child_path = os.path.join(root_folder,_dir)
        if os.path.isdir(child_path):
            if 'en.drg' in os.listdir(child_path):
                compare(child_path)
            else:
                traverse_folder(child_path)

def compare(child_path):
    print child_path
    with codecs.open(os.path.join(child_path,'en.drg'),'rb','utf8') as gold:
        gold_graph = get_triplets(gold)
    with codecs.open(os.path.join(child_path,'en.silver.drg'),'rb','utf8') as silver:
        try:
            silver_graph = get_triplets(silver)
        except:
            #this should trigger when there were errors parsing the silver.drg
            print "Error parsing silver.drg in %s" % child_path
            return
    overlap = len(gold_graph.intersection(silver_graph))/len(list(gold_graph))
    with codecs.open(os.path.join(child_path,'overlap_%s.txt' %MODE),'wb','utf8') as o:
        o.write(unicode(overlap))    

if __name__=="__main__":
    if len(sys.argv)>2:
        MODE = sys.argv[2]
    traverse_folder(sys.argv[1])

from __future__ import division

__author__="Federico Fancellu"

from bs4 import BeautifulSoup
from pymongo import MongoClient

import requests
import codecs
import sys
import time
import os

_CLIENT_ = MongoClient("localhost")
db = _CLIENT_.GMBfiles
collection = db.data

already_visited = []
with codecs.open('../docs_type_info.txt','rb','utf8') as out:
    for line in out:
        if line.startswith('%'):
            par,doc = line[1:].strip().split('\t')
            already_visited.append((par,doc))

def get_all_files(root,res):
    for child in os.listdir(root):
        child_path = os.path.join(root,child)
        if child_path.split('/')[-1].startswith('d'):
            res.append(child_path)
        if os.path.isdir(child_path):
            get_all_files(child_path,res)
    return res

def analyze_page(par,doc,content):
    soup = BeautifulSoup(content)
    bows = filter(lambda x: "id" in x.attrs,soup.find_all("tr"))
    infos = []
    for row in bows:
        cols = row.find_all('td')
        info = {'type_ann':cols[1].text,
                'type_corr':cols[4].text,
                'text':cols[6].text}
        infos.append(info)
    _all = {'par':par,'doc':doc,'info':infos}
    return _all

if __name__=="__main__":
    res_all = get_all_files(sys.argv[1],[])
    for filepath in res_all:
        par, doc = filepath.split('/')[-2:]
        print already_visited[:10]
        if (par,doc) not in already_visited:
            print "Processing %s :: %s " % (par,doc)
            response = requests.get("http://gmb.let.rug.nl/explorer/explore.php?part=%s&doc_id=%s" % (par[1:],doc[1:])+\
            "&type=bows&filter_part=&filter_status=A&filter_subcorpus=&filter_bows=&filter_warnings=")
            entry = analyze_page(par,doc,response.content)
            with codecs.open(sys.argv[2],'a','utf8') as out:
                out.write('%%%s\t%s\n' % (entry['par'],entry['doc']))
                for e in entry['info']:
                    out.write('%s\t%s\t%s\n' % (e['type_ann'],
                                            e['type_corr'],
                                            e['text']))
            time.sleep(1)

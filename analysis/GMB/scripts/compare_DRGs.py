__author__="Federico Fancellu"

import os
import sys
import codecs
import re
import itertools
from collections import Counter
from operator import itemgetter

class DAG(object):
    def __init__(self):
       self.root = None   
       self.graph = {}
 
    def get_triplets(self,text):
        for line in text:
            line = line.strip()
            if line and not line.startswith('%'):
                #(s)tart node, (r)elation, (e)nd node
                s,r,e = line.split()[:3]
                if r not in ['main','referent']:     
                    self.graph.setdefault(s,[]).append(e)

    def get_k_nodes(self):
        k_nodes = [x for x in self.graph if x.startswith('k') and ':' not in x]
        k_nodes_sep = [(x,self.getAllExceptK(x,{})) for x in k_nodes]
        return k_nodes_sep
    
    def getAllExceptK(self, k, subgraph = {}):
        if k in self.graph:
            if self.graph[k]:
                for child in self.graph[k]:
                    if child.startswith('k') and ':' not in child:
                        subgraph.setdefault(k,[])
                    else:
                        subgraph.setdefault(k,[]).append(child)
                        self.getAllExceptK(child,subgraph)
        else: subgraph.setdefault(k,[])
        return subgraph

    def AMRize(self, x, subgraph,varTally={},visitedVars={}):
        def map2char(var_type):
            var_type = var_type.replace('(','')
            if var_type.startswith('k'):
                if var_type.count(':')>0:
                    return var_type.split(':')[-1][0], var_type.split(':')[-1][0], ':V'
                else:
                    return var_type[0],'K',':K'
            if var_type.startswith('c'):
                var,cond = var_type.split(':')[:2]
                return var[0],cond,':C'
        print x
        genericChar, text, _type = map2char(x)
        if x in visitedVars:
            varChar = visitedVars[x]
        else:
            if genericChar in varTally:
                varTally[genericChar] = varTally[genericChar]+1
            else: varTally[genericChar]=1
            varChar = "%s%d" % (genericChar, varTally[genericChar])

        if len(subgraph[x]) == 0:
            if x in visitedVars:
                return "%s %s " % (_type,visitedVars[x])
            else:
                visitedVars[x] = varChar
                return "%s (%s/%s)" % (_type,varChar,varChar[0])
        else:
            return "%s ( %s/%s" % (_type,varChar,text) + " " + "".join(self.AMRize(child,subgraph,varTally,visitedVars) for child in subgraph[x]) + ")"

def graphize(txt): 
   dag = DAG()
   dag.get_triplets(txt)
   return dag

def traverse_folder(root_folder):
    for _dir in os.listdir(root_folder):
        child_path = os.path.join(root_folder,_dir)
        if os.path.isdir(child_path):
            if 'en.drg' in os.listdir(child_path):
                compare(child_path)
            else:
                traverse_folder(child_path)

def amr_matrix_match(gAmrs,sAmrs):
    m = [[0 for x in range(len(sAmrs))] for j in range(len(gAmrs))]
    for ig, g in enumerate(gAmrs):
        for _is, s in enumerate(sAmrs):
            conds_g = re.findall(r'c\d+/([\w\-\.,!\?]*) :V',g,re.IGNORECASE)
            conds_s = re.findall(r'c\d+/([\w\-\.,!\?]*) :V',s,re.IGNORECASE)
            m[ig][_is]= len([i for i in itertools.chain.from_iterable([k] * v for k, v in (Counter(conds_g) & Counter(conds_s)).iteritems())])
    g2s_align = [max(enumerate(r), key=itemgetter(1))[0] for r in m]
    return [(gAmrs[g],sAmrs[s]) for g,s in enumerate(g2s_align)]   

def output(child_path,match_pairs):
    o1 = codecs.open(os.path.join(child_path,'gold.amr'),'wb','utf8')
    o2 = codecs.open(os.path.join(child_path,'silver.amr'),'wb','utf8')
    for gold_amr,silver_amr in match_pairs:
        o1.write(gold_amr+'\n\n')
        o2.write(silver_amr+'\n\n')
    o1.close()
    o2.close()
def compare(child_path):
    print child_path
    with codecs.open(os.path.join(child_path,'en.drg'),'rb','utf8') as gold:
        gold_graph = graphize(gold)
        k_nodes_gold = gold_graph.get_k_nodes()
        k_amr_gold = ["(r/root "+ gold_graph.AMRize(x,subgraph) + ")" for x, subgraph in k_nodes_gold]
    with codecs.open(os.path.join(child_path,'en.silver.drg'),'rb','utf8') as silver:
        silver_graph = graphize(silver)
        k_nodes_silver = silver_graph.get_k_nodes()
        k_amr_silver = ["(r/root "+ silver_graph.AMRize(x,subgraph) + ")" for x, subgraph in k_nodes_silver]
   
    match_pairs = amr_matrix_match(k_amr_gold, k_amr_silver)
    output(child_path,match_pairs)

if __name__=="__main__":    
    traverse_folder(sys.argv[1])

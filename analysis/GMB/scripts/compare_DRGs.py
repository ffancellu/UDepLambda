__author__="Federico Fancellu"

import os
import sys
import codecs

class DAG(object):
    def __init__(self):
       self.nodes = {}
       self.edges = {}
       self.root = None   
    
    def get_triplets(self,text):
        for line in text:
            line = line.strip()
            if line and not line.startswith('%'):
                #(s)tart node, (r)elation, (e)nd node
                s,r,e = line.split()[:3]
                if r not in ['main','referent']:
                    nodeS = Node(s) if s not in self.nodes else self.nodes[s]
                    nodeE = Node(e) if e not in self.nodes else self.nodes[e]
                
                    nodeS.add_child(nodeE)
                    nodeE.add_parent(nodeS)
                
                    if nodeS not in self.nodes: self.nodes[s] = nodeS
                    if nodeE not in self.nodes: self.nodes[e] = nodeE
                
                    self.edges[(nodeS,nodeE)] = r
     
    def determine_root(self):
        for n in self.nodes:
            if not self.nodes[n].parents:
                return self.nodes[n]

class Node(object):
    def __init__(self,name):
        self.name = name
        self.parents = []
        self.children = []

    def __str__(self):
        return self.name

    def add_parent(self,parent_node):
        self.parents.append(parent_node)

    def add_child(self, child_node):
        self.children.append(child_node)
    
    def print_children(self,depth=0):
        print '\t'*depth,self,[x.name for x in self.parents],[x.name for x in self.children]
        depth+=1
        for child in self.children:
            child.print_children(depth)

def AMRize(input_node, visitedVars=[], AMRstr="", depth=0):
    if input_node.name.startswith('k'):
        if ':' not in input_node.name:
            AMRstr+='\t'*depth + ":K (%s / K\n" % input_node.name
        else:
            if input_node.name.count(':')==1:
                k,v = input_node.name.split(":")
                if k+v not in visitedVars:
                    visitedVars.append(k+v) 
                    AMRstr+='\t'*depth + "%s:VAR (%s / %s" % ('\t'*depth,k+v,v[0])
                else: AMRstr+='\t'*depth + "%s:VAR %s" % ('\t'*depth,k+v)
            elif input_node.name.count(':')==2:
                k,p,v = input_node.name.split(':')
                if k+p+v not in visitedVars:
                    visitedVars.append(k+v+p)
                    AMRstr+='\t'*depth + "%s:VAR (%s / %s" % ('\t'*depth,k+p+v,v[0])
                else: AMRstr+='\t'*depth + "%s:VAR %s" % ('\t'*depth,k+p+v)
    if input_node.name.startswith('c'):
        cidx,name= input_node.name.split(':')[:2]
        AMRstr+='\t'*depth + ":C (%s / %s\n" % (cidx,name)
    for child in input_node.children:
        AMRstr = AMRize(child, visitedVars, AMRstr, depth+1)
        AMRstr += ('\t'*depth)+')\n'
    return AMRstr
   

def graphize(txt): 
   dag = DAG()
   dag.get_triplets(txt)
   #print dag.nodes['c28:now:1'].parents[0].name
   return dag.determine_root()
    
def traverse_folder(root_folder):
    for _dir in os.listdir(root_folder):
        child_path = os.path.join(root_folder,_dir)
        if os.path.isdir(child_path):
            if 'en.drg' in os.listdir(child_path):
                compare(child_path)
            else:
                traverse_folder(child_path)

def compare(child_path):
    print os.path.join(child_path,'en.drg')
    with codecs.open(os.path.join(child_path,'en.drg'),'rb','utf8') as gold:
        gold_graph = graphize(gold)
        gold_amr = AMRize(gold_graph)
    with codecs.open(os.path.join(child_path,'en.silver.drg'),'rb','utf8') as silver:
        silver_graph = graphize(silver)
        silver_amr = AMRize(silver_graph)
    o1 = codecs.open('gold','wb','utf8')
    o1.write(gold_amr)
    o1.close()
    o2 = codecs.open('silver','wb','utf8')
    o2.write(silver_amr)
    o2.close()
    sys.exit(0)
if __name__=="__main__":
    traverse_folder(sys.argv[1])

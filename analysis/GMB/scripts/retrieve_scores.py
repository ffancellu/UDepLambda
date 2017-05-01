import codecs
import sys,os
import math

from scipy.stats import pearsonr
from operator import itemgetter

def traverse_dirs(root_dir,collector):
    for _dir in os.listdir(root_dir):
        child_dir = os.path.join(root_dir,_dir)
        if os.path.isdir(child_dir) and 'smatch_score.txt' in os.listdir(child_dir):
            with codecs.open(os.path.join(child_dir,'smatch_score.txt'),'rb','utf8') as scoreAMR,\
                 codecs.open(os.path.join(child_dir,'overlap_hard.txt'),'rb','utf8') as scoreHARD,\
                 codecs.open(os.path.join(child_dir,'overlap_relaxed.txt'),'rb','utf8') as scoreREL:
                amrs = scoreAMR.readlines()
                hards = next(scoreHARD)
                rels = next(scoreREL)
                if amrs:
                    s = amrs[0].strip().split(',')[-1]
                    collector.append((child_dir, float(s),float(hards.strip()),float(rels.strip())))
                else:
                    collector.append((child_dir,None,float(hards.strip()),float(rels.strip())))
        if os.path.isdir(child_dir):
            traverse_dirs(child_dir,collector)
    return collector

def return_list(scores, mean,sd):
    for x,amr,h,r in scores:
       if not amr or amr> (mean+sd):
           print x+ '\t' + 'SILVER'
       elif amr < (mean-sd):
           print x+ '\t' + 'GOLD'

def main(root_dir):
    scores = traverse_dirs(root_dir,[])
    tot_doc = len(scores)
    not_parsed = len([x for x,amr,h,r in scores if not amr])
    print "%%Total documents: %d :: Parsed: %d :: Not parser: %d" % (tot_doc, (tot_doc -not_parsed), not_parsed)
    
    mean = sum([amr for x,amr,h,r in scores if amr])/(tot_doc-not_parsed)
    sd = math.sqrt(sum([(amr-mean)**2 for x,amr,h,r in scores if amr])/(tot_doc-not_parsed))     
    print "%%Mean :: %.3f SD :: %.3f" % (mean,sd)
    
    below = len([amr for x,amr,h,r in scores if amr and amr<(mean-sd)])
    above = len([amr for x,amr,h,r in scores if amr and amr>(mean+sd)])
    
    all_smatch = [amr for x,amr,h,r in scores if amr]
    all_hard_overlap = [h for x,amr,h,r in scores if amr]
    all_soft_overlap = [r for x,amr,h,r in scores if amr]
    amr_hard = pearsonr(all_smatch,all_hard_overlap)
    amr_soft = pearsonr(all_smatch,all_soft_overlap)
    soft_hard = pearsonr(all_hard_overlap, all_soft_overlap)

    print "%%Correlation coeff. between smatch score and hard overlap: %.3f (p:%.4f)" % (amr_hard[0],amr_hard[1])
    print "%%Correlation coeff. between smatch score and soft overlap: %.3f (p:%.4f)" % (amr_soft[0],amr_soft[1])
    print "%%Correlation coeff. between soft overlap and hard overlap: %.3f (p:%.4f)" % (soft_hard[0],soft_hard[1])
    
    return_list(scores,mean,sd)
if __name__=="__main__":
    main(sys.argv[1])

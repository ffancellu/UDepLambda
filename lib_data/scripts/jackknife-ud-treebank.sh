java -cp ../../lib/*:* edu.stanford.nlp.tagger.maxent.MaxentTagger \
-model ../ud-models-v2/en/pos-tagger/utb-caseless-en-bidirectional-glove-distsim-lower.tagger \
-wordFunction edu.stanford.nlp.process.LowercaseFunction \
-textFile format=TSV,wordColumn=1,tagColumn=3,../ud-treebanks-v2.0/UD_English/en-ud-train.uncommented.conllu \
-outputFile ../ud-treebanks-v2.0/UD_English/en-ud-train.jackknifed.tags

python sub_tags.py ../ud-treebanks-v2.0/UD_English/en-ud-train.uncommented.conllu ../ud-treebanks-v2.0/UD_English/en-ud-train.jackknifed.tags ../ud-treebanks-v2.0/UD_English/en-ud-train.jackknifed.conllu


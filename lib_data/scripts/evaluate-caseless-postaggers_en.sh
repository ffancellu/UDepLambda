java -cp ../../lib/*:* edu.stanford.nlp.tagger.maxent.MaxentTagger \
-model ../ud-models-v2/en/pos-tagger/utb-caseless-en-bidirectional-glove-distsim-lower.tagger \
-testFile format=TSV,wordColumn=1,tagColumn=3,../ud-treebanks-v2.0/UD_English/en-ud-dev.uncommented.conllu \
-wordFunction edu.stanford.nlp.process.LowercaseFunction \
2> ../ud-models-v2/en/pos-tagger/utb-caseless-en-bidirectional-glove-distsim-lower.tagger.dev.results.txt

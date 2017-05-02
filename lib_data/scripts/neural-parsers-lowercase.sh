mkdir -p ../ud-models-v2.0/en/neural-parser/

java -cp ../../lib/*:* edu.stanford.nlp.parser.nndep.DependencyParser -trainFile ../ud-treebanks-v2.0/UD_English/en-ud-train.jackknifed.conllu -devFile ../ud-treebanks-v2.0/UD_English/en-ud-dev.jackknifed.conllu -embedFile ../embeddings/glove.6B.50d.txt -embeddingSize 50 -model ud-models-v2.0/en/neural-parser/en-lowercase-glove50.lower.nndep.model.txt.gz

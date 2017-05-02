mkdir -p ud-models-v2/en/pos-tagger/
java -cp ../lib/*:* edu.stanford.nlp.tagger.maxent.MaxentTagger \
        -props props/utb-caseless-en-bidirectional-glove-distsim-lower.tagger.props

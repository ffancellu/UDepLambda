package deplambda.DRTLambda;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;

import java.util.List;

/**
 * Created by ffancellu on 23/04/2017.
 */
public class ParserTest {

    public static void main (String[] args){
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(
                PropertiesUtils.asProperties(
                        "annotators", "tokenize,ssplit,pos,lemma,depparse",
                        "ssplit.isOneSentence", "true",
                        "parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz",
                        "ner.applyNumericClassifiers", "false",
                        "ner.useSUTime", "false",
                        "preprocess.addDateEntities", "true",
                        "preprocess.addNamedEntities", "true",
                        "posTagKey", "UD",
                        "pos.model", "lib_data/ud-models-v1.3/en/pos-tagger/utb-en-bidirectional-glove-distsim-lower.tagger",
                        "depparse.model", "lib_data/ud-models-v2/nndep.model.udv2.emb50.allUniversalPoS.txt.gz",
                        "tokenize.language", "en"));

        // read some text in the text variable
                String text = "Thousands of applicants came from everywhere to try his food ."; // Add your text here!
                Annotation document = new Annotation(text);

        // run all Annotators on this text
                pipeline.annotate(document);

            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

            for(CoreMap sentence: sentences) {
                // traversing the words in the current sentence
                // a CoreLabel is a CoreMap with additional token-specific methods
                for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    // this is the text of the token
                    String word = token.get(CoreAnnotations.TextAnnotation.class);
                    // this is the POS tag of the token
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    // this is the NER label of the token
                    String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                }

                // this is the parse tree of the current sentence
                Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);

                // this is the Stanford dependency graph of the current sentence
                SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
                System.out.println(dependencies.toString());
            }


    }
}

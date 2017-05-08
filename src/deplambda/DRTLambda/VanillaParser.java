package deplambda.DRTLambda;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;
import edu.stanford.nlp.util.Triple;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ffancellu on 26/04/2017.
 */
public class VanillaParser {

    private StanfordCoreNLP pipeline;

    public VanillaParser(){

        // build pipeline
        this.pipeline = new StanfordCoreNLP(
                PropertiesUtils.asProperties(
                        "annotators", "tokenize,ssplit,pos,depparse",
                        "tokenize.language", "en",
                        "tokenize.whitespace", "true",
                        "ssplit.isOneSentence", "true",
                        "preprocess.lowerCase","true",
                        "preprocess.addDateEntities", "true",
                        "preprocess.addNamedEntities", "true",
                        "posTagKey", "UD"));
    }

    public HashMap<Integer, ArrayList<Triple<Integer,String,Integer>>> findDuplicateNodes(String inputSentence) {
        Annotation document = new Annotation(inputSentence);
        // run all Annotators on this text
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences.size()>1){
            throw new ArrayIndexOutOfBoundsException();
        }
        // this is the Stanford dependency graph of the current sentence
        SemanticGraph dependencies = sentences.get(0).get(SemanticGraphCoreAnnotations
                .CollapsedCCProcessedDependenciesAnnotation
                .class);

        System.out.println(dependencies);

        HashMap<Integer,ArrayList<Triple<Integer,String,Integer>>> multipleParents = new HashMap<>();
        for (SemanticGraphEdge edge : dependencies.edgeListSorted()){
            for (SemanticGraphEdge edge2 : dependencies.edgeListSorted()){
                if ((edge.getDependent().index()==edge2.getDependent().index()) &&
                        !edge.equals(edge2)){
                    Triple<Integer,String,Integer> depInfo = Triple.makeTriple(edge.getDependent().index(),
                                                                            edge.getRelation().toString(),
                                                                            edge.getGovernor().index());
                    multipleParents.putIfAbsent(edge.getDependent().index(),new ArrayList<>());
                    if (!multipleParents.get(edge.getDependent().index()).contains(depInfo)) {
                        multipleParents.get(edge.getDependent().index()).add(depInfo);
                    }
                }
            }
        }
        return multipleParents;
    }

    public static void main(String[] args){
        VanillaParser vp = new VanillaParser();
        System.out.println(vp.findDuplicateNodes("The Fox , seeing imminent danger , approached the Lion and promised to contrive for him the capture of the Ass if the Lion would pledge his word not to harm the Fox ."));
    }


}

package deplambda.DRTLambda;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import deplambda.DRT.*;
import deplambda.others.SentenceKeys;
import deplambda.parser.TreeTransformer;
import deplambda.util.DependencyTree;
import deplambda.util.Sentence;
import deplambda.util.TransformationRuleGroups;
import edu.cornell.cs.nlp.spf.mr.lambda.FlexibleTypeComparator;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicLanguageServices;
import edu.cornell.cs.nlp.spf.mr.language.type.MutableTypeRepository;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;
import org.apache.log4j.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Created by ffancellu on 26/04/2017.
 */
public class DRTProcessor {

    public static ArrayList<Triple<String,String,ArrayList<Pair>>> processRoots(
            ArrayList<Triple<String,String,XDRS>> roots) {
        ArrayList<Triple<String,String,ArrayList<Pair>>> allSentsAndDRS = new ArrayList<>();
        for (Triple triplet : roots) {
            XDRS root = (XDRS) triplet.third();
            ArrayList<Pair> sentsAndDRS = DRTSegmenter.extractSentences(root.getTaggedTokens(),
                    root.gatherAllConstituents(new ArrayList<Constituent>()),
                    root.gatherAllRelations(new ArrayList<Relation>()));
            allSentsAndDRS.add(Triple.makeTriple((String)triplet.first(),
                                                (String) triplet.second(),
                                                sentsAndDRS));
        }
        return allSentsAndDRS;

    }

    public static JsonArray jsonifySentsAndRoots(Annotator annotator,
                                                ArrayList<Triple<String,String,ArrayList<Pair>>> allSentsAndDRS){
        JsonArray results = new JsonArray();
        for (Triple triplet: allSentsAndDRS) {
            String par = (String) triplet.first();
            String doc = (String) triplet.second();
            ArrayList<Pair> sentsAndDRSArray = (ArrayList) triplet.third();
            for (int i = 0; i < sentsAndDRSArray.size(); i++) {
                Pair sentAndDRS = sentsAndDRSArray.get(i);
                ArrayList<TaggedToken> tokens = (ArrayList<TaggedToken>) sentAndDRS.first();
                //            get the sentence as string
                String sentence = tokens.stream().map(x -> x.toString()).collect(Collectors.joining(" "));
                JsonObject jsonSentence = new JsonObject();
                jsonSentence.addProperty("sentence", sentence);
                try {
                    annotator.parseSentence(jsonSentence);
                } catch(java.lang.IllegalArgumentException e){
                    System.err.println("Inconsistent number of already tokenized words, and newly tokenized words. Skipping.");
                    continue;
                }

                JsonArray depInfo = extractDepInfo(jsonSentence);
                JsonArray GMBInfo = extractGMBInfo((DRTElement) sentAndDRS.second());

                JsonObject sentenceAnn = new JsonObject();
                sentenceAnn.add("dep", depInfo);
                sentenceAnn.add("GMB", GMBInfo);
                sentenceAnn.add("Annotations", new JsonArray());
                sentenceAnn.addProperty("sent_id", i);
                sentenceAnn.addProperty("par", par);
                sentenceAnn.addProperty("doc", doc);
                sentenceAnn.addProperty("sentence", sentence);

                results.add(sentenceAnn);
            }
        }
        return results;
    }

    private static JsonArray extractDepInfo(JsonObject jsonSentence){
        JsonArray result = new JsonArray();
        JsonArray words = jsonSentence.getAsJsonArray("words");
        int idxAnn = 1;
        for (Object wordObj: words){
            JsonObject annotationWord = new JsonObject();
            JsonObject annotationEdge = new JsonObject();
            JsonObject word = (JsonObject) wordObj;
            if (word.get("head").getAsInt()>0){
                JsonObject parent = (JsonObject) words.get(word.get("head").getAsInt()-1);
                annotationWord.addProperty("parent", parent.get("word").getAsString() + "-" +
                        parent.get("dep").getAsString());
                annotationEdge.addProperty("parent",parent.get("word").getAsString());

            } else {
                annotationWord.addProperty("parent", "ROOT");
            }

            String token = word.get("word").getAsString();
            String isNer = word.get("ner").getAsString();
            String dep = word.get("dep").getAsString();
            int idx = word.get("index").getAsInt();


            annotationWord.addProperty("name",token);
            annotationWord.addProperty("isNer",isNer);
            annotationWord.addProperty("index",idxAnn);
            annotationWord.addProperty("indexSentence",idx);
            annotationWord.addProperty("type","word");

            idxAnn++;

            annotationEdge.addProperty("name", dep);
            annotationEdge.addProperty("index",idxAnn);
            annotationEdge.addProperty("type","edge");

            idxAnn++;

            result.add(annotationWord);
            result.add(annotationEdge);
        }
        return result;
    }

    private static JsonArray extractGMBInfo(DRTElement drg){
        JsonArray result = new JsonArray();
        int idxAnn  = 1;
        for(String s: drg.gatherGraphTriplets(new ArrayList<>())){
            String name = s.split("-> ")[1].trim();
            JsonObject triplet = new JsonObject();
            triplet.addProperty("name",name.trim());
            triplet.addProperty("type",s);
            triplet.addProperty("index",idxAnn);

            result.add(triplet);

            idxAnn++;
        }
        return result;
    }
}

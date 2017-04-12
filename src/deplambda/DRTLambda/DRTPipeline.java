package deplambda.DRTLambda;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import deplambda.DRT.Constituent;
import deplambda.DRT.DRTSegmenter;
import deplambda.DRT.Relation;
import deplambda.DRT.XDRS;
import deplambda.others.NlpPipeline;
import deplambda.util.XMLReader;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.Triple;
import org.apache.jena.atlas.lib.Tuple;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import deplambda.util.Sentence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ffancellu on 20/02/2017.
 */
public class DRTPipeline {

//    static MutableTypeRepository types;
    static final int BATCHSIZE = 50;
    private NlpPipeline pipelineOne, pipelineTwo, pipelineThree, pipelineFour;


    public DRTPipeline(Map<String,String> options) throws Exception {
//        activate the four UDepLambda pipelines
        System.err.println("Loading pipelines...");
        pipelineOne = new NlpPipeline(getOptionOne(options));
        System.err.println("1st pipeline loaded!");
        pipelineTwo = new NlpPipeline(getOptionTwo(options));
        System.err.println("2nd pipeline loaded!");
        pipelineThree = new NlpPipeline(getOptionThree(options));
        System.err.println("3th pipeline loaded!");
        pipelineFour = new NlpPipeline(getOptionFour(options));
        System.err.println("4th pipeline loaded!");
    }

    public void processFolder(File rootDir) throws IOException, SAXException, ParserConfigurationException {
        String[] subDirs = rootDir.list();
        ArrayList<Tuple> accumulator = new ArrayList<Tuple>();
        for (String subDirPath: subDirs) {
            File child = new File(rootDir, subDirPath);
            if (child.isDirectory()) {
                processFolder(child);
            } else if (child.toString().endsWith("drs.xml")) {
                System.out.println("Parsing: " + subDirPath + " " + child.toString());
                XDRS root = XMLReader.parseXML(child);
                System.out.println("Segmenting the XDRS into sentences...");
                DRTSegmenter.extractSentences(root.getTaggedTokens(),
                        root.gatherAllConstituents(new ArrayList<Constituent>()),
                        root.gatherAllRelations(new ArrayList<Relation>()));
//                System.out.println("Extracting triplets...");
//                ArrayList<Tuple> tuples = root.segmentConstituents();
//                System.out.println("Processing triplets...");
//                ArrayList<Triple> triplets = processTuples(tuples);
//                System.out.println("Processing done");
//                for (Triple t: triplets){
//                    System.out.println(t.first());
//                    ((XDRS)t.second()).Graph2AMR(new StringBuilder(),0, new HashMap<>(), new ArraySet<>());
//                    ((XDRS)t.third()).Graph2AMR(new StringBuilder(),0, new HashMap<>(), new ArraySet<>());
//                }
            }
        }
    }

//    private ArrayList<Triple> processTuples(ArrayList<Tuple> DRTs){
//        ArrayList<Triple> xdrsList = new ArrayList<>();
////        take the sentences and parse it
//        JsonParser JSONparser = new JsonParser();
//        for (Tuple tuple: DRTs){
//            JsonObject sentenceJson = JSONparser.parse(String.format("{\"sentence\":\"%s\"}",tuple.get(0).toString().replace("\"",""))).getAsJsonObject();
//            pipelineOne.processSentence(sentenceJson);
//            pipelineTwo.processSentence(sentenceJson);
//            pipelineThree.processSentence(sentenceJson);
//            pipelineFour.processSentence(sentenceJson);
////        for each lambda expression in the forest create a DRTElement and serialize it
////            for (Object sentence: forestList){
////                JsonObject sentenceJson = (JsonObject) sentence;
////            String sentString = sentenceJson.get("sentence").getAsString();
////            System.out.println(sentString);
////            String sentObliq = sentenceJson.get("deplambda_oblique_tree").getAsString();
////            System.out.println(sentObliq);
//            String sentLambda = sentenceJson.get("deplambda_expression").getAsString();
////            System.out.println(sentLambda);
//            XDRS autoDRT = Lambda2DRT.transform(
//                    new Sentence(sentenceJson),
//                    LogicalExpression.read(sentLambda),
//                    false);
//            xdrsList.add(Triple.makeTriple(tuple.get(0), autoDRT,tuple.get(1)));
//            }
//        return xdrsList;
//        }

    public static Map<String,String> getOptionOne(Map<String,String> options){
        Map<String,String> newOptions =
                ImmutableMap.of(
                        "annotators",
                        options.get("annotators1"),
                        "tokenize.language",
                        options.get("tokenize.language"),
                        "ner.applyNumericClassifiers",
                        options.get("ner.applyNumericClassifiers"),
                        "ner.useSUTime",
                        options.get("ner.useSUTime")
                );
        return newOptions;
    }

    private static Map<String,String> getOptionTwo(Map<String,String> options){
        Map<String,String> newOptions =
                ImmutableMap.of(
                        "preprocess.addDateEntities",
                        options.get("preprocess.addDateEntities"),
                        "preprocess.addNamedEntities",
                        options.get("preprocess.addNamedEntities"),
                        "annotators",
                        options.get("annotators2"),
                        "tokenize.whitespace",
                        options.get("tokenize.whitespace2"),
                        "ssplit.eolonly",
                        options.get("ssplit.eolonly2"));
        return newOptions;
    }

    private static Map<String,String> getOptionThree(Map<String,String> options){
        Map<String,String> newOptions = new HashMap<>();
        newOptions.put("preprocess.lowerCase",options.get("preprocess.lowerCase"));
        newOptions.put("annotators",options.get("annotators3"));
        newOptions.put("tokenize.whitespace", options.get("tokenize.whitespace3"));
        newOptions.put("ssplit.eolonly", options.get("ssplit.eolonly3"));
        newOptions.put("languageCode", options.get("languageCode3"));
        newOptions.put("posTagKey", options.get("posTagKey"));
        newOptions.put("pos.model", options.get("pos.model"));
        newOptions.put("depparse.model", options.get("depparse.model"));
        return newOptions;
    }

    private static Map<String,String> getOptionFour(Map<String,String> options){
        Map<String,String> newOptions = new HashMap<>();
        newOptions.put("annotators",options.get("annotators4"));
        newOptions.put("tokenize.whitespace", options.get("tokenize.whitespace4"));
        newOptions.put("ssplit.eolonly",options.get("ssplit.eolonly4"));
        newOptions.put("languageCode", options.get("languageCode4"));
        newOptions.put("deplambda", options.get("deplambda"));
        newOptions.put("deplambda.definedTypesFile", options.get("deplambda.definedTypesFile"));
        newOptions.put("deplambda.treeTransformationsFile", options.get("deplambda.treeTransformationsFile"));
        newOptions.put("deplambda.relationPrioritiesFile", options.get("deplambda.relationPrioritiesFile"));
        newOptions.put("deplambda.lambdaAssignmentRulesFile", options.get("deplambda.lambdaAssignmentRulesFile"));
        newOptions.put("deplambda.lexicalizePredicates", options.get("deplambda.lexicalizePredicates"));
        newOptions.put("deplambda.debugToFile", options.get("deplambda.debugToFile"));

        return newOptions;
    }

    public static void main(String[] args) throws Exception {
//        if (args.length == 0 || args.length % 2 != 0) {
//            System.err
//                    .println("Specify pipeline arguments, e.g., annotator, languageCode, preprocess.capitalize. See the NlpPipelineTest file.");
//            System.exit(0);
//        }
//        Map<String, String> options = new HashMap<>();
//        for (int i = 0; i < args.length; i += 2) {
//            options.put(args[i], args[i + 1]);
//        }

        Map<String,String> options = new HashMap<>();
//        TODO: add the options here for testing
        options.clear();
        options.put("annotators1", "tokenize,ssplit,pos,lemma,ner");
        options.put("tokenize.language", "en");
        options.put("ner.applyNumericClassifiers", "false");
        options.put("ner.useSUTime", "false");
        options.put("preprocess.addDateEntities", "true");
        options.put("preprocess.addNamedEntities", "true");
        options.put("annotators2", "tokenize,ssplit");
        options.put("tokenize.whitespace2", "true");
        options.put("ssplit.eolonly2", "true");
        options.put("preprocess.lowerCase","true");
        options.put("annotators3", "tokenize,ssplit,pos,depparse");
        options.put("tokenize.whitespace3", "true");
        options.put("ssplit.eolonly3", "true");
        options.put("languageCode3", "en");
        options.put("posTagKey", "UD");
        options.put("pos.model", "lib_data/ud-models-v1.3/en/pos-tagger/utb-caseless-en-bidirectional-glove-distsim-lower.tagger");
        options.put("depparse.model", "lib_data/ud-models-v1.3/en/neural-parser/en-lowercase-glove50.lower.nndep.model.txt.gz");
        options.put("annotators4","tokenize,ssplit");
        options.put("tokenize.whitespace4", "true");
        options.put("ssplit.eolonly4", "true");
        options.put("languageCode4", "en");
        options.put("deplambda", "true");
        options.put("deplambda.definedTypesFile", "lib_data/UDLambdaNeg/ud.types.enh.txt");
        options.put("deplambda.treeTransformationsFile", "lib_data/UDLambdaNeg/ud-tree-transformation-rules.proto.enh.txt");
        options.put("deplambda.relationPrioritiesFile", "lib_data/UDLambdaNeg/ud-relation-priorities.proto.enh.txt");
        options.put("deplambda.lambdaAssignmentRulesFile", "lib_data/UDLambdaNeg/ud-lambda-assignment-rules.proto.enh.txt");
        options.put("deplambda.lexicalizePredicates", "true");
        options.put("deplambda.debugToFile", "debug.txt");


        DRTPipeline drtPipeline = new DRTPipeline(options);
        File rootDir = new File("/Users/ffancellu/Documents/Research/Resource/gmb-2.2.0/data/");
        drtPipeline.processFolder(rootDir);
    }

}

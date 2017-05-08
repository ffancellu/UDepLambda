package deplambda.DRTLambda;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hp.hpl.jena.sparql.function.library.strjoin;
import deplambda.DRT.DRTElement;
import deplambda.DRT.XDRS;
import deplambda.others.NlpPipeline;
import deplambda.others.SentenceKeys;
import deplambda.util.DependencyTree;
import deplambda.util.MongoDumper;
import edu.cornell.cs.nlp.spf.mr.lambda.FlexibleTypeComparator;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicLanguageServices;
import edu.cornell.cs.nlp.spf.mr.language.type.MutableTypeRepository;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.util.ArrayUtils;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;


import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by ffancellu on 20/02/2017.
 */
public class DRTPipeline {

    static MutableTypeRepository types;

    static {
        try {
            DependencyTree.LEXICAL_KEY = SentenceKeys.LEMMA_KEY;
            types = new MutableTypeRepository("lib_data/UDLambdaNeg/ud.types.enh.txt");

            LogicLanguageServices.setInstance(new LogicLanguageServices.Builder(
                    types, new FlexibleTypeComparator()).closeOntology(false)
                    .setNumeralTypeName("i").build());

        } catch (IOException e) {
            e.printStackTrace();
        }
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

        Map<String, String> options = new HashMap<>();
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
        options.put("preprocess.lowerCase", "true");
        options.put("annotators3", "tokenize,ssplit,pos,depparse");
        options.put("tokenize.whitespace3", "true");
        options.put("ssplit.eolonly3", "true");
        options.put("languageCode3", "en");
        options.put("posTagKey", "UD");
        options.put("pos.model", "lib_data/ud-models-v2.0/en/pos-tagger/utb-caseless-en-bidirectional-glove-distsim-lower.tagger");
        options.put("depparse.model", "lib_data/ud-models-v2.0/en/neural-parser/en-lowercase-glove50.lower.alluni.nndep.model.txt.gz");
        options.put("folder.chunks", "10");
        options.put("debug", "false");


        Annotator annotator = new Annotator(options);

        File rootDir = new File("/Users/ffancellu/Documents/Research/Resource/gmb-2.2.0/data");

        if (options.get("debug").startsWith("true")) {
            String[] debugArgs = options.get("debug").split("_");
            File testDir = Paths.get(rootDir.toString(), "p" + debugArgs[1], "d" + debugArgs[2]).toFile();
            ArrayList<Triple<String, String, XDRS>> roots = XMLExtractor.processOne(testDir);
            ArrayList<Triple<String, String, ArrayList<Pair>>> processedRoots = DRTProcessor.processRoots(roots);
            for (Triple triplet : processedRoots) {
                for (Pair pair : (ArrayList<Pair>) triplet.third()) {
                    ((DRTElement) pair.second()).printChildren(0);
                }
            }
        } else {
            int x = Integer.parseInt(options.get("folder.chunks"));
            List<String> subFolders = Arrays.asList(rootDir.list());
            List<List<String>> folderChunks = new ArrayList<>();
            if (x > 0) {
                for (int i = 0; i < subFolders.size(); i += x) {
                    if (i + x > subFolders.size()) {
                        folderChunks.add(subFolders.subList(i, subFolders.size()));
                    } else {
                        folderChunks.add(subFolders.subList(i, i + x));
                    }
                }
            } else if (x == 0) {
                folderChunks.add(subFolders);
            } else {
                throw new ArrayIndexOutOfBoundsException();
            }
            for (List<String> folderChunk : folderChunks) {
                ArrayList<Triple<String, String, XDRS>> roots = XMLExtractor.traverseFolder(rootDir,
                        folderChunk);
                System.out.println("Segmenting the XDRS into sentences...");
                ArrayList<Triple<String, String, ArrayList<Pair>>> processedRoots = DRTProcessor.processRoots(roots);
                System.out.println("Jsonifying objects...");
                JsonArray DRTObjs = DRTProcessor.jsonifySentsAndRoots(annotator, processedRoots);
                System.out.println("Dump into mongoDB...");
                MongoDumper.dumpJsonArray(DRTObjs, "dep2GMB", "data");
            }
        }
    }

}

package deplambda.DRTLambda;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import deplambda.others.NlpPipeline;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ffancellu on 23/04/2017.
 */
public class Annotator {

    private NlpPipeline pipelineOne, pipelineTwo, pipelineThree;

    public Annotator(Map<String,String> options) throws Exception {
//        activate the four UDepLambda pipelines
        System.err.println("Loading pipelines...");
        pipelineOne = new NlpPipeline(getOptionOne(options));
        pipelineTwo = new NlpPipeline(getOptionTwo(options));
        pipelineThree = new NlpPipeline(getOptionThree(options));
    }


    private Map<String,String> getOptionOne(Map<String,String> options){
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

    private Map<String,String> getOptionTwo(Map<String,String> options){
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

    private Map<String,String> getOptionThree(Map<String,String> options){
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

    public void parseSentence(JsonObject sentence){
        this.pipelineOne.processIndividualSentence(sentence);
        this.pipelineTwo.processIndividualSentence(sentence);
        this.pipelineThree.processIndividualSentence(sentence);
    }
}

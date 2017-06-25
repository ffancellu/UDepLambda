package deplambda.DRTLambda;

import com.google.gson.Gson;
import deplambda.DRT.Constituent;
import deplambda.DRT.DRTElement;
import deplambda.DRT.Relation;
import deplambda.DRT.TaggedToken;
import edu.stanford.nlp.util.Pair;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by ffancellu on 02/04/2017.
 */
public class DRTSegmenter {

    static Gson gson = new Gson();

    public static HashMap<Integer, ArrayList<TaggedToken>> indexSentences(SortedMap<String, TaggedToken> tokenMap){
        HashMap<Integer,ArrayList<TaggedToken>> sentences = new HashMap<>();
        for (String tokIdx: tokenMap.keySet()){
            int sentIdx = Integer.parseInt(tokIdx.substring(1,2));
            sentences.putIfAbsent(sentIdx,new ArrayList<>());
            sentences.get(sentIdx).add(tokenMap.get(tokIdx));
        }
        return sentences;
    }

    public static HashMap<Integer, ArrayList<Constituent>> indexConstituents(ArrayList<Constituent> constituents){
        HashMap<Integer,ArrayList<Constituent>> constituent2sents = new HashMap<>();
        for (Constituent constituent: constituents){
//            TODO: if we want to be more strict make sure that the entire constituent is on one sentence and one sentence only.
            int sentIdx = constituent.assign2Sentence();
            if (sentIdx!=-2) {
                constituent2sents.putIfAbsent(sentIdx, new ArrayList<>());
                constituent2sents.get(sentIdx).add(constituent);
            }
        }
        return constituent2sents;
    }

    public static HashMap<Integer, ArrayList<Relation>> indexRelations(ArrayList<Relation> relations,
                                                                       HashMap<Integer,ArrayList<Constituent>> constituents){
        HashMap<Integer, ArrayList<Relation>> relation2sents = new HashMap<>();
//        create a mapping constituent to sentIdx they refer to
        HashMap<String,ArrayList<Integer>> const2sent = new HashMap<>();
        for (int constIdx: constituents.keySet()){
            for (Constituent c: constituents.get(constIdx)){
                const2sent.putIfAbsent(c.getLabel(), new ArrayList<>());
                const2sent.get(c.getLabel()).add(constIdx);
            }
        }
//        TODO: get the relations and their constituent arguments;
//        1) first we get the relations that are *not* of type 'continuation' and assign this relation iff the two
//        constituent arguments belong to the same sentence
        for (Relation rel: relations) {
            String arg1 = rel.getChildTags().get(0);
            String arg2 = rel.getChildTags().get(1);
            if (!rel.getName().equals("continuation")) {
                if (const2sent.get(arg1).equals(const2sent.get(arg2))) {
                    for (int s : const2sent.get(arg1)) {
                        relation2sents.putIfAbsent(s, new ArrayList<>());
                        relation2sents.get(s).add(rel);
                    }
                }
            } else {
//        2) 'continuation' might break sentence boundaries, so just add a 'continuation' with one argument.
//                FOR THIS MOMENT THIS IS IGNORED
            }
        }

        return relation2sents;
    }

    public static void assignUndecidedConstituents(HashMap<Integer,ArrayList<Constituent>> consts,
                                                   ArrayList<Relation> relations){
        if (consts.containsKey(-1)){
            //        get all constituents assigned to -1
            ArrayList<Constituent> unassingedConsts = consts.get(-1);
            //          invert consts to Const label to int
            HashMap<String,Integer> invertedConsts = new HashMap<>();
            for (Map.Entry<Integer,ArrayList<Constituent>> entry: consts.entrySet()){
                for (Constituent c: entry.getValue()){
                    invertedConsts.put(c.getLabel(),entry.getKey());
                }
            }
            //        for each relation if a unassigned const in included in the args, assign it to sentence of the arg it shares a relation with
            for (Relation rel: relations){
                String relConst1 = rel.getChildTags().get(0);
                String relConst2 = rel.getChildTags().get(1);
                for (Constituent unassignedConst: unassingedConsts){
                    if (unassignedConst.getLabel().equals(relConst1) && invertedConsts.containsKey(relConst2)){
                        int sentIdx = invertedConsts.get(relConst2);
                        consts.get(sentIdx).add(unassignedConst);
                    }
                    if (unassignedConst.getLabel().equals(relConst2) && invertedConsts.containsKey(relConst1)){
                        int sentIdx = invertedConsts.get(relConst1);
                        consts.get(sentIdx).add(unassignedConst);
                    }
                }
            }
        }
    }


    public static ArrayList<Constituent> filterConstituents(ArrayList<Constituent> consts){
        ArrayList<Constituent> constsCopy = (ArrayList<Constituent>) consts.clone();
        for (Constituent c: consts){
            ArrayList<Constituent> subConsts = c.getSubConstituents(new ArrayList<>());
            for (Constituent subConst: subConsts){
                if (constsCopy.contains(subConst)){constsCopy.remove(subConst);}
            }
        }
        return constsCopy;
    }

    public static ArrayList<Relation> filterRelations(ArrayList<Relation> rels,
                                       ArrayList<Constituent> filteredConsts){
        List<String> filteredConstsLabels = filteredConsts.stream().map(x-> x.getLabel()).collect(Collectors.toList());
        ArrayList<Relation> relCopy = (ArrayList<Relation>) rels.clone();
        for (Relation rel: rels){
            if (!(filteredConstsLabels.contains(rel.getChildTags().get(0)) &&
            filteredConstsLabels.contains(rel.getChildTags().get(1)))){
                relCopy.remove(rel);
            }
        }
        return relCopy;
    }

    public static DRTElement assignRootElement(ArrayList<Constituent> constituents,
                                               ArrayList<Relation> relations){
        DRTElement root = new DRTElement();
        if (relations.isEmpty()){
            if (constituents.size()>1){
                HashMap<String,DRTElement> childConsts = new HashMap<>();
                for (Constituent c: constituents){
                    childConsts.put(c.getLabel(),c);
                }
                Relation rootRel = new Relation("continuation",childConsts);
                for (Constituent c: constituents){
                    rootRel.setParentChildRel(c);
                }
                root = rootRel;
            } else {
                if (constituents.get(0).getChildren().size()==1){
                    root = constituents.get(0).getChildren().get(0);
                } else {
                    root = constituents.get(0);
                }
            }
        } else {
            if (constituents.size()/relations.size()!=2){
//                ignore the sentence
            } else {
                HashMap<String,DRTElement> childConsts = new HashMap<>();
                childConsts.put(constituents.get(0).getLabel(),constituents.get(0));
                childConsts.put(constituents.get(1).getLabel(),constituents.get(1));
                Relation rootRel = new Relation(relations.get(0).getName(),childConsts);
                rootRel.setParentChildRel(constituents.get(0));
                rootRel.setParentChildRel(constituents.get(1));
                root = rootRel;
            }
        }
        return root;
    }

    public static void fixInternalRelations(DRTElement c) {
//                gather the constituent in this yield
        List<Integer> consts2string = c.gatherAllConstituents(new ArrayList<>()).stream().map(x -> Integer.parseInt(x.getLabel().substring(1))).collect(Collectors.toList());
        Collections.sort(consts2string);
////            collect pairs of contiguous constituent indeces
        List<Pair> constTuples = IntStream.range(1, consts2string.size())
                .mapToObj(i -> new Pair(consts2string.get(i-1), consts2string.get(i)))
                .collect(Collectors.toList());
        c.readjustRelations(constTuples);

    }

    public static ArrayList<Pair> extractSentences(SortedMap<String,TaggedToken> tokenMap,
                                                   ArrayList<Constituent> constituents,
                                                   ArrayList<Relation> relations){
        ArrayList<Pair> sentsAndDRS = new ArrayList<>();
        if (!constituents.isEmpty()) {
            HashMap<Integer, ArrayList<TaggedToken>> sentences = indexSentences(tokenMap);
            HashMap<Integer, ArrayList<Constituent>> constituentsMap = indexConstituents(constituents);
            HashMap<Integer, ArrayList<Relation>> relationMap = indexRelations(relations, constituentsMap);
            //        if there exists some constituents that could not be assigned, assign them to the constituent in the closest relationship with
            assignUndecidedConstituents(constituentsMap, relations);
            for (int i : constituentsMap.keySet()) {
                ArrayList<TaggedToken> sentence = sentences.get(i);
//                make sure there is a sentence to start with
                if (sentence==null){
                    continue;
                }
                /**
                 * if B is dependant on A, we disregard B as an independent constituent.
                 */
                ArrayList<Constituent> sentenceConstituents = filterConstituents(constituentsMap.get(i));
                ArrayList<Relation> rootRelations = relationMap.containsKey(i) ?
                        filterRelations(relationMap.get(i), sentenceConstituents) :
                        new ArrayList<>();
                DRTElement root = assignRootElement(sentenceConstituents, rootRelations);
                //            create relation children of internal SDRS
                fixInternalRelations(root);
                root.removeUnaryConstituents();
                root.removeConditionSDRS();
                root.assignIndicesDRSs(new int[] {0}, new ArrayList<>());
                sentsAndDRS.add(Pair.makePair(sentence,root));
            }

        }
        return sentsAndDRS;
    }


}

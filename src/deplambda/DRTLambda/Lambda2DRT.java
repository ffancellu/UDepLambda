package deplambda.DRTLambda;

import com.google.common.collect.ImmutableMap;
import deplambda.DRT.*;
import deplambda.others.PredicateKeys;
import deplambda.parser.PostProcessLogicalForm;
import deplambda.util.Sentence;
import edu.cornell.cs.nlp.spf.mr.lambda.*;
import edu.stanford.nlp.util.ArraySet;
import jregex.Matcher;
import jregex.Pattern;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Created by ffancellu on 11/03/2017.
 */
public class Lambda2DRT extends PostProcessLogicalForm{

    static Pattern EVENT_ID_PATTERN = new Pattern(String.format(
            "%sw-([0-9]+)-.*", PredicateKeys.EVENT_PREFIX));
    static Pattern TYPE_ID_PATTERN = new Pattern(String.format(
            "%sw-([0-9]+)-.*", PredicateKeys.TYPE_PREFIX));
    static Pattern TYPEMOD_ID_PATTERN = new Pattern(String.format(
            "%sw-([0-9]+)-.*", PredicateKeys.TYPEMOD_PREFIX));
    static Pattern EVENTMOD_ID_PATTERN = new Pattern(String.format(
            "%sw-([0-9]+)-.*", PredicateKeys.EVENTMOD_PREFIX));

    static final List<String> relations = Arrays.asList("parallel");
    static final List<String> unaryOperators = Arrays.asList("neg");
    static final List<String> binaryOperators = Arrays.asList("imp");


    public static XDRS transform(Sentence sentence, LogicalExpression parse,
                                      boolean lexicalizePredicates) {
        List<Literal> mainPredicates = new ArrayList<>();
        Map<Term, List<Integer>> varToEvents = new HashMap<>();
        Map<Term, List<Integer>> varToEntities = new HashMap<>();
        Map<Term, List<Integer>> varToTypemodEntities = new HashMap<>();
        List<Pair<Term, Term>> equalPairs = new ArrayList<>();

        process(mainPredicates, varToEvents, varToEntities, varToTypemodEntities, equalPairs, sentence, parse);

        getCleanVarToEntities(varToEntities,sentence);
        getCleanVarToEntities(varToTypemodEntities,sentence);
        getCleanVarToEvents(varToEvents,sentence);
        getPopulateEquals(equalPairs, varToEntities, varToEvents);

        XDRS superRoot = new XDRS();
        DRS root = new DRS();
        superRoot.setParentChildRel(root);

        iterateDRTGraph(parse, varToEvents, varToEntities, varToTypemodEntities, sentence, lexicalizePredicates, root);
        superRoot.printChildren(0);
//        superRoot.polishRelationNodes(0);
        return superRoot;
    }

    private static void processPredicate(Literal literal,
                                         Map<Term, List<Integer>> varToEvents,
                                         Map<Term, List<Integer>> varToEntities,
                                         Map<Term, List<Integer>> varToTypemodEntities,
                                         List<Pair<Term, Term>> equalPairs) {
        String predicate = ((LogicalConstant) literal.getPredicate()).getBaseName();
        if (predicate.startsWith(PredicateKeys.EVENT_PREFIX)
                || predicate.startsWith(PredicateKeys.EVENTMOD_PREFIX)) {
            // (p_EVENT_w-2-nominate:u $0:<a,e>)
            Matcher matcher;
            if (predicate.startsWith(PredicateKeys.EVENT_PREFIX)) {
                matcher = EVENT_ID_PATTERN.matcher(predicate);
            } else {
                matcher = EVENTMOD_ID_PATTERN.matcher(predicate);
            }
            matcher.matches();
            int eventId = Integer.parseInt(matcher.group(1));
            Term key = (Term) literal.getArg(0);
            varToEvents.putIfAbsent(key, new ArrayList<>());
            varToEvents.get(key).add(eventId - 1);

        } else if (predicate.startsWith(PredicateKeys.TYPE_PREFIX)) {
            Matcher matcher = TYPE_ID_PATTERN.matcher(predicate);
            matcher.matches();
            int typeId = Integer.parseInt(matcher.group(1));
            Term key = (Term) literal.getArg(0);
            varToEntities.putIfAbsent(key, new ArrayList<>());
            varToEntities.get(key).add(typeId - 1);
        } else if (predicate.startsWith(PredicateKeys.TYPEMOD_PREFIX)){
            Matcher matcher = TYPEMOD_ID_PATTERN.matcher(predicate);
            matcher.matches();
            int typeId = Integer.parseInt(matcher.group(1));
            Term key = (Term) literal.getArg(0);
            varToTypemodEntities.putIfAbsent(key, new ArrayList<>());
            varToTypemodEntities.get(key).add(typeId - 1);
        } else if (predicate.startsWith(PredicateKeys.EQUAL_PREFIX)) {
            Term arg1 = (Term) literal.getArg(0);
            Term arg2 = (Term) literal.getArg(1);
            equalPairs.add(Pair.of(arg1, arg2));
        }
    }

    /**
     * Makes predicates readable.
     *
     * @param mainPredicate the main predicates that are to be cleaned, i.e., the
     *        predicates that start with "p_"
     * @param varToEvents a map containing a mapping between event lambda
     *        variables and their source word's index
     * @param varToEntities a map containing a mapping between event lambda
     *        variables and their source word's index
     * @param sentence the source sentence
     * @param lexicalizePredicates lexicalize predicates by appending the event,
     *        e.g., eat(1:e) ^ arg1(1:e , 1:x) becomes eat.arg1(1:e , 1:x)
     * @return the set of cleaned predicates
     */
    private static Set<String> createConditions(
            Literal mainPredicate,
            Map<Term, List<Integer>> varToEvents,
            Map<Term, List<Integer>> varToEntities,
            Map<Term, List<Integer>> varToTypemodEntities,
            Sentence sentence,
            boolean lexicalizePredicates,
            DRS drs) {
        Set<String> cleanedPredicates = new HashSet<>();
        String basePredicate =
                ((LogicalConstant) mainPredicate.getPredicate()).getBaseName();

        if (basePredicate.startsWith(PredicateKeys.EVENT_ENTITY_PREFIX)) {
            // (p_EVENT.ENTITY_l-nmod.w-4-as:b $0:<a,e> $1:<a,e>)
            String cleanedPredicate =
                    basePredicate.substring(PredicateKeys.EVENT_ENTITY_PREFIX.length());
            cleanedPredicate = getGetCleanedBasePredicate(cleanedPredicate);
            Term eventTerm = (Term) mainPredicate.getArg(0);
            Term entityTerm = (Term) mainPredicate.getArg(1);
//                if the event and the entity hasn't been found in the list we don't store it
            if (varToEvents.containsKey(eventTerm)
                    && varToEntities.containsKey(entityTerm)) {
                for (int eventIndex : varToEvents.get(eventTerm)) {
                    String lexicalizedEvent =
                            !lexicalizePredicates || getIsNamedEntity(sentence, eventIndex) ? ""
                                    : sentence.getLemma(eventIndex) + ".";
                    for (int entityIndex : varToEntities.get(entityTerm)) {
                        DRTVariable DRTVarEvent = drs.getOrCreateVariable(eventTerm,"e", eventIndex);
                        DRTVariable DRTVarEntity = drs.getOrCreateVariable(entityTerm,"x",entityIndex);
                        drs.addBinaryPredicate(DRTVarEvent,DRTVarEntity,cleanedPredicate);
                    }
                }
            }
        } else if (basePredicate.startsWith(PredicateKeys.ENTITY_ENTITY_PREFIX)) {
            // (p_EVENT.ENTITY_l-nmod.w-4-as:b $0:<a,e> $1:<a,e>)
            String cleanedPredicate =
                    basePredicate.substring(PredicateKeys.ENTITY_ENTITY_PREFIX.length());
            cleanedPredicate = getGetCleanedBasePredicate(cleanedPredicate);
            Term entityTerm1 = (Term) mainPredicate.getArg(0);
            Term entityTerm2 = (Term) mainPredicate.getArg(1);
//                if the event and the entity hasn't been found in the list we don't store it
            if ((varToEntities.containsKey(entityTerm1) && varToTypemodEntities.containsKey(entityTerm2))) {
                for (int entityIndex1 : varToEntities.get(entityTerm1)) {
                    for (int entityIndex2 : varToTypemodEntities.get(entityTerm2)) {
                        DRTVariable DRTVarEntityOne = drs.getOrCreateVariable(entityTerm1, "x", entityIndex1);
                        DRTVariable DRTVarEntityTwo = drs.getOrCreateVariable(entityTerm2, "x", entityIndex2);
                        drs.addBinaryPredicate(DRTVarEntityOne, DRTVarEntityTwo, cleanedPredicate);
                    }
                }
            }
        } else if (basePredicate.startsWith(PredicateKeys.EVENT_EVENT_PREFIX)) {
            String cleanedPredicate =
                    basePredicate.substring(PredicateKeys.EVENT_EVENT_PREFIX.length());
            cleanedPredicate = getGetCleanedBasePredicate(cleanedPredicate);
            Term eventTerm1 = (Term) mainPredicate.getArg(0);
            Term eventTerm2 = (Term) mainPredicate.getArg(1);
            if (varToEvents.containsKey(eventTerm1) && varToEvents.containsKey(eventTerm2)) {
                for (int eventIndex1 : varToEvents.get(eventTerm1)) {
                    for (int eventIndex2 : varToEvents.get(eventTerm2)) {
                        DRTVariable DRTVarEventOne = drs.getOrCreateVariable(eventTerm1, "e", eventIndex1);
                        DRTVariable DRTVarEventTwo = drs.getOrCreateVariable(eventTerm2, "e", eventIndex2);
                        drs.addBinaryPredicate(DRTVarEventOne, DRTVarEventTwo, cleanedPredicate);
                    }
                }
            }
        } else if (!lexicalizePredicates
                    && basePredicate.startsWith(PredicateKeys.EVENT_PREFIX)) {
                // (p_EVENT_w-5-judge:u $0:<a,e>)
                String cleanedPredicate =
                        basePredicate.substring(PredicateKeys.EVENT_PREFIX.length());
                cleanedPredicate = getGetCleanedBasePredicate(cleanedPredicate);
                Term eventTerm = (Term) mainPredicate.getArg(0);
                if (varToEvents.containsKey(eventTerm)) {
                    for (int eventIndex : varToEvents.get(eventTerm)) {
                        DRTVariable DRTVarEvent = drs.getOrCreateVariable(eventTerm,"e", eventIndex);
                        if (!getIsNamedEntity(sentence, eventIndex)) {
//                            System.out.println("EVENT : " + cleanedPredicate);
                            drs.addUnaryPredicate(DRTVarEvent,cleanedPredicate,null);
                        } else {
//                            TODO: add extra info if required, otherwise unify
//                            System.out.println("Named entity found : " + cleanedPredicate);
                            drs.addUnaryPredicate(DRTVarEvent, cleanedPredicate, null);
                        }
                    }
                }
        } else if (basePredicate.startsWith(PredicateKeys.COUNT_PREFIX)) {
//                 (p_COUNT:b $0:<a,e> $1:<a,e>)
            Term countTerm = (Term) mainPredicate.getArg(0);
            Term resultTerm = (Term) mainPredicate.getArg(1);
            if (varToEntities.containsKey(countTerm)
                    && varToEntities.containsKey(resultTerm)) {
                for (int countTermIndex : varToEntities.get(countTerm)) {
                    for (int resultTermIndex : varToEntities.get(resultTerm)) {
                        DRTVariable DRTVarEntity = drs.getOrCreateVariable(resultTerm, "e", resultTermIndex);
                        DRTVariable DRTVarCardinal = drs.getOrCreateVariable(countTerm, "x", countTermIndex);
                        drs.addBinaryPredicate(DRTVarEntity, DRTVarCardinal, "card");
                    }
                }
            }
        } else if (basePredicate.startsWith(PredicateKeys.TYPE_PREFIX)) {
//                // (p_TYPE_w-8-court:u $0:<a,e>)
            Matcher matcher = TYPE_ID_PATTERN.matcher(basePredicate);
            matcher.matches();
            int typeIsFromIndex = Integer.parseInt(matcher.group(1)) - 1;

            String cleanedPredicate =
                    basePredicate.substring(PredicateKeys.TYPE_PREFIX.length());
            cleanedPredicate = getGetCleanedBasePredicate(cleanedPredicate);

            Term entityTerm = (Term) mainPredicate.getArg(0);
            if (varToEntities.containsKey(entityTerm)) {
                for (int entityIndex : varToEntities.get(entityTerm)) {
                    DRTVariable DRTVarEntity = drs.getOrCreateVariable(entityTerm, "x", entityIndex);
                    if (!getIsNamedEntity(sentence, entityIndex)
                            || typeIsFromIndex != entityIndex) {
//                        System.out.println("TYPE : " + cleanedPredicate);
                        drs.addUnaryPredicate(DRTVarEntity, cleanedPredicate, null);
                    } else {
//                        System.out.println("Named entity found : " + cleanedPredicate);
                        drs.addUnaryPredicate(DRTVarEntity, cleanedPredicate, null);
                    }
                }
            }
        } else if (basePredicate.startsWith(PredicateKeys.TYPEMOD_PREFIX)) {
            // (p_TYPEMOD_w-8-red:u $0:<a,e>)
            Matcher matcher = TYPEMOD_ID_PATTERN.matcher(basePredicate);
            matcher.matches();

            String cleanedPredicate =
                    basePredicate.substring(PredicateKeys.TYPEMOD_PREFIX.length());
            cleanedPredicate = getGetCleanedBasePredicate(cleanedPredicate);
            Term entityTerm = (Term) mainPredicate.getArg(0);
            if (varToTypemodEntities.containsKey(entityTerm)) {
                for (int entityIndex : varToTypemodEntities.get(entityTerm)) {
                    DRTVariable DRTVarEntity = drs.getOrCreateVariable(entityTerm, "x", entityIndex);
                    if (!getIsNamedEntity(sentence, entityIndex)) {
//                        System.out.println("TYPEMOD : " + cleanedPredicate);
                        drs.addUnaryPredicate(DRTVarEntity, cleanedPredicate, null);
                    } else {
//                        System.out.println("Named entity found : " + cleanedPredicate);
                        drs.addUnaryPredicate(DRTVarEntity, cleanedPredicate, null);
                    }
                }
            }
        }else if (basePredicate.startsWith(PredicateKeys.EVENTMOD_PREFIX)) {
            // (p_EVENTMOD_w-8-red:u $0:<a,e>)
            Matcher matcher = EVENTMOD_ID_PATTERN.matcher(basePredicate);
            matcher.matches();

            String cleanedPredicate =
                    basePredicate.substring(PredicateKeys.EVENTMOD_PREFIX.length());
            cleanedPredicate = getGetCleanedBasePredicate(cleanedPredicate);
            Term argTerm = (Term) mainPredicate.getArg(0);
            if (varToEvents.containsKey(argTerm)) {
                for (int eventIndex : varToEvents.get(argTerm)) {
//                    System.out.println("EVENTMOD : " + cleanedPredicate);
                    DRTVariable DRTVarEntity = drs.getOrCreateVariable(argTerm, "e", eventIndex);
                    drs.addUnaryPredicate(DRTVarEntity, cleanedPredicate, null);
                }
            }
        }

        return cleanedPredicates;
    }

    private static void createRelation(Literal parse,
                                      Map<Term, List<Integer>> varToEvents,
                                      Map<Term, List<Integer>> varToEntities,
                                        Map<Term, List<Integer>> varToTypemodEntities,
                                      Sentence sentence,
                                      DRTElement drtElement, boolean lexicalizedPredicates,
                                      String tag){

        if (parse.numArgs()!=2){
            throw new ArrayIndexOutOfBoundsException();
        }

        HashMap<String,DRTElement> args = new HashMap<>();
        args.put("arg0",new DRS());
        args.put("arg1",new DRS());
        Relation rel = new Relation(tag,args);
        rel.addChildrenConstituents();

        drtElement.setParentChildRel(rel);

        iterateDRTGraph(parse.getArg(0), varToEvents, varToEntities,varToTypemodEntities,
                sentence, lexicalizedPredicates, rel.getArgs().get("arg0"));
        iterateDRTGraph(parse.getArg(1), varToEvents, varToEntities,varToTypemodEntities,
                sentence, lexicalizedPredicates, rel.getArgs().get("arg1"));

    }

    private static void createUnaryOperator(Literal parse, Map<Term, List<Integer>> varToEvents,
                                            Map<Term, List<Integer>> varToEntities,
                                            Map<Term, List<Integer>> varToTypemodEntities,
                                            Sentence sentence,
                                            DRS drs, boolean lexicalizePredicates, String tag) {
        Condition cond = new Condition();
        cond.setName(tag);

        drs.setParentChildRel(cond);
        cond.setParentChildRel(new DRS());

        if (parse.numArgs()!=1 || cond.getChildren().size()!=1){
            throw new ArrayIndexOutOfBoundsException();
        }

        iterateDRTGraph(parse.getArg(0), varToEvents, varToEntities, varToTypemodEntities,
                    sentence,lexicalizePredicates, cond.getChildren().get(0));


    }

    private static void createBinaryOperator(Literal parse,
                                             Map<Term, List<Integer>> varToEvents,
                                             Map<Term, List<Integer>> varToEntities,
                                             Map<Term, List<Integer>> varToTypemodEntities,
                                             Sentence sentence, DRS drs,
                                             boolean lexicalizePredicates, String tag) {
//        the parent is always a DRS, the operator being a condition
        Condition cond = new Condition();
        cond.setName(tag);

        drs.setParentChildRel(cond);
        cond.setParentChildRel(new DRS());
        cond.setParentChildRel(new DRS());

        if (parse.numArgs()!=2 || cond.getChildren().size()!=2){
            throw new ArrayIndexOutOfBoundsException();
        }

        for (int i=0;i<cond.getChildren().size();i++){
            iterateDRTGraph(parse.getArg(i), varToEvents, varToEntities, varToTypemodEntities,
                    sentence,lexicalizePredicates, cond.getChildren().get(i));
        }
    }

    private static void process(List<Literal> mainPredicates,
                                Map<Term, List<Integer>> varToEvents,
                                Map<Term, List<Integer>> varToEntities,
                                Map<Term, List<Integer>> varToTypemodEntities,
                                List<Pair<Term, Term>> equalPairs,
                                Sentence sentence,
                                LogicalExpression parse) {
        if (parse instanceof Lambda) {
            process(mainPredicates, varToEvents, varToEntities, varToTypemodEntities,
                    equalPairs, sentence, ((Lambda) parse).getBody());
        } else if (parse instanceof Literal) {
            if (! ((Literal) parse).getPredicate().toString().startsWith("$")) {
                if (((LogicalConstant) ((Literal) parse).getPredicate()).getBaseName()
                        .startsWith("p_")) {
                    mainPredicates.add(((Literal) parse));
                    processPredicate(((Literal) parse), varToEvents, varToEntities, varToTypemodEntities, equalPairs);
                } else {
                    for (int i = 0; i < ((Literal) parse).numArgs(); i++) {
                        process(mainPredicates, varToEvents, varToEntities, varToTypemodEntities,
                                equalPairs, sentence, ((Literal) parse).getArg(i));
                    }
                }
            }
        }
    }

    private static void iterateDRTGraph(LogicalExpression parse,
                                        Map<Term, List<Integer>> varToEvents,
                                        Map<Term, List<Integer>> varToEntities,
                                        Map<Term, List<Integer>> varToTypemodEntities,
                                        Sentence sentence,
                                        boolean lexicalizePredicates,
                                        DRTElement drtElement) {
        if (parse instanceof Lambda) {
            iterateDRTGraph(((Lambda) parse).getBody(), varToEvents, varToEntities, varToTypemodEntities, sentence,
                    lexicalizePredicates, drtElement);
        } else if (parse instanceof Literal) {
            if (! ((Literal) parse).getPredicate().toString().startsWith("$")) {
                if (((LogicalConstant) ((Literal) parse).getPredicate()).getBaseName()
                        .startsWith("p_")) {
                    createConditions((Literal) parse, varToEvents, varToEntities, varToTypemodEntities,
                            sentence,lexicalizePredicates,(DRS) drtElement);
                } else if (relations.contains(((LogicalConstant) ((Literal) parse)
                        .getPredicate()).getBaseName())){
                    createRelation((Literal) parse, varToEvents, varToEntities, varToTypemodEntities,
                            sentence, drtElement, lexicalizePredicates, "parallel");
                } else if (binaryOperators.contains(((LogicalConstant) ((Literal) parse)
                        .getPredicate()).getBaseName())){
                    createBinaryOperator((Literal) parse, varToEvents, varToEntities, varToTypemodEntities,
                            sentence, (DRS) drtElement, lexicalizePredicates, "imp");
                } else if (unaryOperators.contains(((LogicalConstant) ((Literal) parse)
                        .getPredicate()).getBaseName())){
                    createUnaryOperator((Literal) parse, varToEvents, varToEntities, varToTypemodEntities,
                            sentence, (DRS) drtElement, lexicalizePredicates, "neg");
                } else {
                    for (int i = 0; i < ((Literal) parse).numArgs(); i++) {
                        iterateDRTGraph(((Literal) parse).getArg(i), varToEvents, varToEntities, varToTypemodEntities,
                                 sentence, lexicalizePredicates, drtElement);
                    }
                }
            }
        }

    }

}

package deplambda.DRT;


import com.google.common.collect.ImmutableMap;
import edu.cornell.cs.nlp.spf.mr.lambda.Term;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by ffancellu on 08/02/2017.
 */
public class DRS extends DRTElement{

    private String label;
    private Set<DRTVariable> domain;
    private Set<Condition> conditions;
    private HashMap<String,DRTVariable> variables;

    public DRS(){
        label = new String();
        domain = new HashSet<>();
        conditions = new HashSet<>();
        variables = new HashMap<>();
    }

    public Set<DRTVariable> getDomain(){
        return domain;
    }

    public HashMap<String,DRTVariable> getVariablesMap() {return variables;}

    public Set<Condition> getConditions(){
        return conditions;
    }

    public void addUnaryPredicate(DRTVariable var, String predicate, Map<String,String> extra) {
        Map<String, DRTVariable> yield = ImmutableMap.of("arg0", var);
        Condition unaryCondition = new Condition(predicate, yield, extra);
        conditions.add(unaryCondition);
        this.setParentChildRel(unaryCondition);
    }

    public void addBinaryPredicate(DRTVariable event, DRTVariable entity, String predicate){
        Map<String, DRTVariable> yield = ImmutableMap.of("arg0", event, "arg1",entity);
        Condition binaryCondition = new Condition(predicate, yield, null);
        conditions.add(binaryCondition);
        this.setParentChildRel(binaryCondition);
    }

    public DRTVariable getOrCreateVariable(Term term, String type, int idx){
        DRTVariable var = new DRTVariable(term,type,idx);
        if (!domain.contains(var)){
            domain.add(var);
        } else {
            for (DRTVariable v : domain) {
                if (v.equals(var)) {
                    var = v;
                }
            }
        }
        return var;
    }

    @Override
    public void parseContent(Node node, Map<String,DRTVariable> allVars) {
        label = node.getAttributes().getNamedItem("label").getNodeName();
        NodeList drsChildren = node.getChildNodes();
        for (int i = 0; i < drsChildren.getLength(); i++) {
            Node n = drsChildren.item(i);
            if (n.getNodeName().equals("domain")) {
                NodeList variables = n.getChildNodes();
                for (int jv = 0; jv < variables.getLength(); jv++) {
                    Node varNode = variables.item(jv);
                    if (varNode.getNodeName().equals("dr")) {
                        DRTVariable var = new DRTVariable();
//                        this.setParentChildRel(var);
                        var.parseContent(varNode);
                        this.variables.put(String.format("%s%d", var.getType(), var.getIdx()), var);
                    }
                }
            } else if (n.getNodeName().equals("conds")) {
                NodeList conditions = n.getChildNodes();
                for (int jc = 0; jc < conditions.getLength(); jc++) {
                    Node condNode = conditions.item(jc);
                    if (condNode.getNodeName().equals("cond")) {
//                        System.out.println("CONDITION");
                        Condition cond = new Condition();
                        this.setParentChildRel(cond);
                        cond.parseContent(condNode, allVars);
                        this.conditions.add(cond);
                    }
                }
            }
        }
    }

}

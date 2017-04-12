package deplambda.DRT;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ffancellu on 11/02/2017.
 */
public class Condition extends DRTElement{

    private Map<String,DRTVariable> yield;
//    for extra information e.g. class and symbols of NEs
    private Map<String,String> extra;
    private String name;

    public Condition(String name, Map<String, DRTVariable> yield, Map<String,String> extra){
        this.name = name;
        this.yield = yield;
        if (extra==null) {
            this.extra = new HashMap<String, String>();
        } else {this.extra = extra;}
    }

    public Condition(){
        name = new String();
        yield = new HashMap<>();
        extra = new HashMap<>();
    }

    public void setName(String name){this.name = name;}
    public String getName(){return this.name;}

    public Map<String,DRTVariable> getVarYield(){return this.yield;}

    private static DRTVariable fetchVariable(String entityName, Map<String,DRTVariable> vars){
        DRTVariable resVar = null;
        for (String varName: vars.keySet()){
            String fullname = String.format("%s%d",vars.get(varName).getType(),vars.get(varName).getIdx());
            if (fullname.equals(entityName)) {
                resVar = vars.get(varName);
                break;
            }
        }
        return resVar;
    }

    @Override
    public void parseContent(Node node, Map<String,DRTVariable> parentVars){
        Node condNode = node.getChildNodes().item(1);
        switch(condNode.getNodeName()){
            case "named":
                processNamed(condNode, parentVars);
                break;
            case "timex":
                processTimex(condNode,parentVars);
                break;
            case "pred":
                processPred(condNode,parentVars);
                break;
            case "rel":
                processRel(condNode,parentVars);
                break;
            case "eq":
                processEq(condNode,parentVars);
                break;
            case "card":
                processCard(condNode,parentVars);
                break;
            case "whq":
            case "not":
            case "binary":
            case "necessity":
            case "p":
            case "pos":
            case "or":
            case "imp":
            case "prop":
            case "nec":
            case "duplex":
                processNested(condNode, parentVars);
                break;
            default:
                break;
        }
//        get the tokId for the condition
        NodeList condChildren = condNode.getChildNodes();
        for (int j=0;j<condChildren.getLength();j++){
            if (condChildren.item(j).getNodeName().equals("indexlist")){
                NodeList indices = ((Element)condChildren.item(j)).getElementsByTagName("index");
                for (int i=0;i<indices.getLength();i++){
                    this.addToken(indices.item(i).getTextContent());
                }
            }
        }
    }

    private void processNamed(Node node, Map<String,DRTVariable> vars){
//        # NAMED ENTITIES (e.g. John)
//        # named -> arg, symbol, class, type :: named(arg,symbol,class)
        name = node.getNodeName();
        String entity = node.getAttributes().getNamedItem("arg").getNodeValue();
        DRTVariable matchVar = Condition.fetchVariable(entity,vars);
        yield.put("arg0",matchVar);
        extra.put("symbol", node.getAttributes().getNamedItem("symbol").getNodeValue());
        extra.put("class", node.getAttributes().getNamedItem("class").getNodeValue());
    }

    private void processTimex(Node node, Map<String,DRTVariable> vars){
//        # TIME & DATES (e.g. august 2004)
//        # timex -> arg, iter_date :: timex(arg,date.txt)
        name = node.getNodeName();
        String entity = node.getAttributes().getNamedItem("arg").getNodeValue();
        DRTVariable matchVar = Condition.fetchVariable(entity,vars);
        yield.put("arg0",matchVar);
        String date = node.getOwnerDocument().getElementsByTagName("date").item(0).getFirstChild().getNodeValue();
        extra.put("date",date);
    }

    private void processPred(Node node, Map<String,DRTVariable> vars) {
//        # EXISTENTIAL PREDICATION
//        # pred -> arg, symbol, type, sense
//        # type is the POS tag, sense is an integer
        name = node.getAttributes().getNamedItem("symbol").getNodeValue();
        String entity = node.getAttributes().getNamedItem("arg").getNodeValue();
        DRTVariable matchVar = Condition.fetchVariable(entity,vars);
        yield.put("arg0",matchVar);
        extra.put("type",node.getAttributes().getNamedItem("type").getNodeValue());
        extra.put("sense",node.getAttributes().getNamedItem("sense").getNodeValue());
    }

    private void processRel(Node node, Map<String,DRTVariable> vars) {
//        # RELATIONS (e.g. Actor(x,y))
//        # rel -> arg1, arg2, argn??, symbol, sense
        name = node.getAttributes().getNamedItem("symbol").getNodeValue();
        String entity1 = node.getAttributes().getNamedItem("arg1").getNodeValue();
        DRTVariable entity1Var = Condition.fetchVariable(entity1,vars);
        String entity2 = node.getAttributes().getNamedItem("arg2").getNodeValue();
        DRTVariable entity2Var = Condition.fetchVariable(entity2,vars);
        yield.put("arg1",entity1Var);
        yield.put("arg2",entity2Var);
        extra.put("sense",node.getAttributes().getNamedItem("sense").getNodeValue());
    }

    private void processEq(Node node, Map<String,DRTVariable> vars) {
//        # EQUALITIES
//        # eq -> arg1 = arg2
        name = node.getNodeName();
        String arg1 = node.getAttributes().getNamedItem("arg1").getNodeValue();
        DRTVariable arg1Var = Condition.fetchVariable(arg1,vars);
        String arg2 = node.getAttributes().getNamedItem("arg2").getNodeValue();
        DRTVariable arg2Var = Condition.fetchVariable(arg2,vars);
        yield.put("arg1",arg1Var);
        yield.put("arg2",arg2Var);
    }

    private void processCard(Node node, Map<String,DRTVariable> vars) {
//        # CARDINAL NUMBERS
//        # card -> card(arg, value)
        name = node.getNodeName();
        String arg1 = node.getAttributes().getNamedItem("arg").getNodeValue();
        DRTVariable arg0Var = Condition.fetchVariable(arg1,vars);
        String arg2 = node.getAttributes().getNamedItem("value").getNodeValue();
        yield.put("arg0",arg0Var);
        extra.put("arg2",arg2);
    }

    private void processNested(Node node, Map<String,DRTVariable> vars) {
        name = node.getNodeName();
        NodeList condChildren = node.getChildNodes();
        for (int i = 0; i< condChildren.getLength();i++){
            Node childNode = condChildren.item(i);
            if (childNode.getNodeName().equals("drs")){
                DRS drs = new DRS();
                drs.setParent(this);
                this.addChild(drs);
                drs.parseContent(childNode, vars);
            } else if (childNode.getNodeName().equals("sdrs")){
                SDRS sdrs= new SDRS(false);
                this.setParentChildRel(sdrs);
                sdrs.parseContent(childNode, vars);
            }
        }
    }

    @Override
    public String toString(){
        return name + " " + yield.toString();
    }

}

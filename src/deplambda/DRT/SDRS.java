package deplambda.DRT;


import edu.stanford.nlp.util.ArraySet;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ffancellu on 21/02/2017.
 */
public class SDRS extends DRTElement {

    private Map<String,Constituent> constituents;
    private ArraySet<Relation> relations;
//    first if direct child of a XDRS node
    private boolean first;

    public SDRS(boolean first){
        first = first;
        constituents = new HashMap<>();
        relations = new ArraySet<>();
    }

    public Map<String,Constituent> getConstituents(){
        return this.constituents;
    }
    public void addRelation(Relation r){ this.relations.add(r);}
    public ArraySet<Relation> getRelations(){return this.relations;}

    public ArraySet<DRTElement> getRelatedConstituents(){
        ArrayList<String> visitedConst = new ArrayList<>();
        ArraySet<DRTElement> res = new ArraySet<>();
//        First iterate through complex relations and gather residual Constituents
//        TODO: if the constituent has a nested sdrs
        for (Relation rel:relations) {
            if (!rel.getName().equals("continuation")) {
                rel.addChildrenConstituents();
                res.add(rel);
                for (String key: rel.getArgs().keySet()) {
                   visitedConst.add(((Constituent)rel.getArgs().get(key)).getLabel());
                }
            }
        }
        for (Relation rel:relations) {
            if (rel.getName().equals("continuation")) {
                if (!visitedConst.contains(((Constituent) rel.getArgs().get("arg1")).getLabel())){
                    XDRS xdrs = new XDRS();
                    xdrs.setParentChildRel(rel.getArgs().get("arg1"));
                    res.add(xdrs);
                }
                if (!visitedConst.contains(((Constituent) rel.getArgs().get("arg2")).getLabel())) {
                    XDRS xdrs = new XDRS();
                    xdrs.setParentChildRel(rel.getArgs().get("arg2"));
                    res.add(xdrs);
                }
            }
        }
        return res;
    }

    public DRTElement addOrGetChild() {
        DRTElement target;
        if (!this.getChildren().isEmpty()) {
            target = this.getChildren().get(this.getChildren().size() - 1);
        } else {
            target = new DRS();
            this.getChildren().add(target);
        }
        return target;
    }


    @Override
    public void parseContent(Node node, Map<String, DRTVariable> allVars){
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i< childNodes.getLength();i++) {
            Node n = childNodes.item(i);
            if (n.getNodeName().equals("relations")) {
                NodeList relNodes = n.getChildNodes();
                for (int r = 0; r < relNodes.getLength(); r++) {
                    Node relNode = relNodes.item(r);
                    if (relNode.getNodeName().equals("drel")) {
                        Relation rel = new Relation();
                        rel.parseContent(relNode);
                        relations.add(rel);
                    }
                }
            }
        }
        for (int i = 0; i< childNodes.getLength();i++) {
            Node n = childNodes.item(i);
            if (n.getNodeName().equals("constituents")) {
//               traverse the constituent nodes
                NodeList constNodes = n.getChildNodes();
                for (int j1 = 0; j1 < constNodes.getLength(); j1++) {
                    Node constChild = constNodes.item(j1);
//                    "SUB" deals with constituents in subordinate relation
                    if (constChild.getNodeName().equals("sub")) {
                        for (int s=0;s<constChild.getChildNodes().getLength();s++){
                            Node subChild = constChild.getChildNodes().item(s);
                            if (subChild.getNodeName().equals("constituent")){
                                Constituent c = new Constituent();
                                c.parseContent(subChild, allVars);
                                this.setParentChildRel(c);
                                constituents.put(c.getLabel(), c);
                            }
                        }
                    }
                    if (constChild.getNodeName().equals("constituent")) {
                        Constituent c = new Constituent();
                        c.parseContent(constChild, allVars);
                        this.setParentChildRel(c);
                        constituents.put(c.getLabel(), c);
                    }
                }
            }
        }
    }

}
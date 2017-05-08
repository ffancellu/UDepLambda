package deplambda.DRT;

import com.sun.tools.internal.jxc.ap.Const;
import edu.cornell.cs.nlp.spf.mr.lambda.Term;
import edu.stanford.nlp.util.ArraySet;
import edu.stanford.nlp.util.Pair;
import joptsimple.internal.Strings;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import java.lang.reflect.Array;
import java.rmi.activation.ActivationGroup_Stub;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ffancellu on 11/02/2017.
 */
public class DRTElement {

    private DRTElement parent;
    private ArrayList<DRTElement> children;
    private ArraySet<String> tokIds;

    public DRTElement(){
        this.children = new ArrayList<>();
        this.tokIds = new ArraySet<>();
    }

    public void addChild(DRTElement child){
        children.add(child);
    }
    public void setChildren(ArrayList<DRTElement> children) {this.children = children;}

    public void addToken(String tokenName) {this.tokIds.add(tokenName);}

    public void setParent(DRTElement parent) { this.parent = parent;}

    public void setParentChildRel(DRTElement child){
        children.add(child);
        child.setParent(this);
    }

    public ArrayList<DRTElement> getChildren(){
        return children;
    }

    public ArraySet<String> getTokIds(){return tokIds;}

    public DRTElement getParent() {return parent;}

    public void parseContent(Node node, Map<String,DRTVariable> allVars){}
    public void parseContent(Node node){}

    /**
     * Retrieve all Constituents in the yield of an DRTElement
     */
    public ArrayList<Constituent> getSubConstituents(ArrayList<Constituent> consts){
        for (DRTElement child: this.getChildren()){
            if (child instanceof Constituent){
                consts.add((Constituent) child);
            }
            child.getSubConstituents(consts);
        }
        return consts;
    }

    /**
     * Recursively retrieve all constituents under the yield of a XDRS element
     * @return ArrayList<Constituent>
     */

    public ArrayList<Constituent> gatherAllConstituents(ArrayList<Constituent> constituents){
        for (DRTElement child: this.getChildren()){
            if (child instanceof SDRS){
                constituents.addAll(((SDRS) child).getConstituents().values());
            }
            child.gatherAllConstituents(constituents);
        }
        return constituents;
    }

    /**
     * Recursively retrieve all relations under the yield of a XDRS element
     * @return ArrayList<Relation>
     */

    public ArrayList<Relation> gatherAllRelations(ArrayList<Relation> rels){
        if (this instanceof SDRS){
            SDRS currentSDRS = (SDRS) this;
            rels.addAll(currentSDRS.getRelations());
        }
        for (DRTElement child: this.getChildren()){
            child.gatherAllRelations(rels);
        }
        return rels;
    }

    /**
     * Retrieve all the taggedTokens indices in the yield of an DRTElement
     * @return TreeSet<String>
     */
    public TreeSet<String> getIndicesYield(TreeSet<String> indicesYield){
        if (this instanceof Condition || this instanceof DRTVariable){
            indicesYield.addAll(this.getTokIds());
        }
        for (DRTElement child: this.getChildren()){
            child.getIndicesYield(indicesYield);
        }
        return indicesYield;
    }

    public void printChildren(int depth){
        System.out.println(Strings.repeat('\t',depth) + this.toString() + " " + this.getChildren());//+ " " + (this.parent!=null? this.parent.toString():"null"));
        if (this instanceof DRS){
            System.out.print(" " + ((DRS) this).getIndexDRS());
        }
        for(DRTElement child:children){
            child.printChildren(depth+1);
        }
    }

    /**
     * Assign indices to DRS and SDRS elements
     */
    public void assignIndicesDRSs(int[] tally,
                                  ArrayList<DRTElement> visited){
        for (DRTElement child: this.getChildren()){
            if (child instanceof DRS){
                tally[0]++;
                ((DRS) child).setIndexDRS(tally[0]);
                visited.add(child);
            } else if (child instanceof SDRS){
                tally[0]++;
                ((SDRS) child).setIndexSDRS(tally[0]);
                visited.add(child);
            }
            child.assignIndicesDRSs(tally,visited);
        }
    }

    private void createRelationSubtree(Relation rel, Constituent c1, Constituent c2){
        //                add them as child of the relation
        rel.setParentChildRel(c1);
        rel.setParentChildRel(c2);
        this.setParentChildRel(rel);
        //                remove the constituents as children of the SDRS
        this.getChildren().remove(c1);
        this.getChildren().remove(c2);
    }

    public void readjustRelations(List<Pair> allowedConsts){
        if (this instanceof SDRS){
//            take the relations
            ArrayList<Relation> rels = ((SDRS) this).getRelations();
            Map<String,Constituent> consts = ((SDRS) this).getConstituents();
//            if the relation is ! continuation ->
            ArrayList<Relation> relsFiltered = rels.stream().filter(x -> !x.getName().equals("continuation")).collect(Collectors.toCollection(ArrayList::new));
            Deque<Relation> relations = new ArrayDeque<>();
            for (Pair p : allowedConsts){
//                System.out.println("current pair " + p);
                String value1 = "k" + String.valueOf(p.first());
                String value2 = "k" + String.valueOf(p.second());
                if (consts.keySet().contains(value1) && consts.keySet().contains(value2)){
                    for (int i = 0; i< relsFiltered.size();i++){
                        Relation rel = relsFiltered.get(i);
                        if (rel.getChildTags().get(0).equals(value1) &&
                                rel.getChildTags().get(1).equals(value2)){
//                            System.out.println("Rel triggered for " + value1 + " " + value2);
                            Constituent c1 = consts.get("k" + String.valueOf(p.first()));
                            Constituent c2 = consts.get("k" + String.valueOf(p.second()));
                            //                add them as child of the relation
                            rel.setParentChildRel(c1);
                            rel.setParentChildRel(c2);
                            //                remove the constituents as children of the SDRS
                            this.getChildren().remove(c1);
                            this.getChildren().remove(c2);
//                            check whether we can unify the relations;
                            if (!relations.isEmpty()){
                                Relation lastRel = relations.pop();
//                                System.out.println("last rel " + lastRel.getChildTags());
//                                System.out.println("rel " + rel.getChildTags());
                                if (lastRel.getChildTags().get(1).equals(rel.getChildTags().get(0))){
//                                    remove the second child
//                                    System.out.println("making the big change");
                                    DRTElement lastChild = lastRel.getChildren().get(lastRel.getChildren().size()-1);
                                    lastRel.getChildren().remove(lastChild);
//                                    add the rel as child
                                    lastRel.setParentChildRel(rel);
                                    relations.add(lastRel);
                                } else {
                                    relations.add(rel);
                                }
                            } else {relations.add(rel);}

                        }
                    }
                }
            }
            for (Relation r: relations){
                this.setParentChildRel(r);
            }

        }
        for (DRTElement el: this.getChildren()){
            el.readjustRelations(allowedConsts);
        }
    }

    public void removeUnaryConstituents(){
        ArrayList<DRTElement> unaryNodes = new ArrayList<>();
        ArrayList<DRTElement> unaryChildren = new ArrayList<>();
        for (DRTElement child: this.getChildren()){
            if (child instanceof Constituent && child.getChildren().size()==1){
                unaryNodes.add(child);
                for (int i=0;i<child.getChildren().size();i++){
                    DRTElement grandChild = child.getChildren().get(i);
                    grandChild.setParent(this);
                    unaryChildren.add(grandChild);
                }
            }
        }
        this.getChildren().removeAll(unaryNodes);
        this.getChildren().addAll(unaryChildren);

        for (DRTElement child: this.getChildren()){
            child.removeUnaryConstituents();
        }
    }

    public void removeConditionSDRS(){
        ArrayList<DRTElement> unaryNodes = new ArrayList<>();
        ArrayList<DRTElement> unaryChildren = new ArrayList<>();
        for (DRTElement c: this.getChildren()){
            if (c instanceof SDRS && this instanceof Condition){
                unaryNodes.add(c);
                for (int i=0;i<c.getChildren().size();i++){
                    DRTElement grandChild = c.getChildren().get(i);
                    grandChild.setParent(this);
                    unaryChildren.add(grandChild);
                }
            }
        }
        this.getChildren().removeAll(unaryNodes);
        this.getChildren().addAll(unaryChildren);

        for (DRTElement child: this.getChildren()){
            child.removeConditionSDRS();
        }
    }


    public String Graph2AMR(StringBuilder sb,
                            int depth,
                            HashMap<String,Integer> varTally,
                            ArraySet<String> visitedVar) {
//        (c / contrast-01
//          :ARG2 (s / surprise-01 :polarity -
//                :ARG0 (t / that)
//                :ARG1 (i / i)
//                :degree (m / much)
//                :ARG1-of (r / real-04)))
        if (this instanceof XDRS) {
            sb.append(Strings.repeat('\t', depth) + "(b0 / XDRS\n");
        } else if(this instanceof Constituent){
            varTally.putIfAbsent("constituent",1);
            sb.append(Strings.repeat('\t',depth) + String.format(":K (k%d / K\n", varTally.get("constituent")));
            varTally.put("constituent",varTally.get("constituent")+1);
        } else if(this instanceof DRS){
            varTally.putIfAbsent("drs",1);
            sb.append(Strings.repeat('\t',depth) + String.format(":DRS (d%d / DRS\n", varTally.get("drs")));
            varTally.put("drs",varTally.get("drs")+1);
        } else if(this instanceof SDRS) {
            varTally.putIfAbsent("sdrs",1);
            sb.append(Strings.repeat('\t', depth) + String.format(":SDRS (s%d / SDRS\n", varTally.get("sdrs")));
            varTally.put("sdrs",varTally.get("sdrs")+1);
        } else if(this instanceof Relation){
            varTally.putIfAbsent("rel",1);
            sb.append(Strings.repeat('\t',depth) + String.format(":REL (r%d /%s\n", varTally.get("rel"), ((Relation) this).getName()));
            varTally.put("rel",varTally.get("rel")+1);
        } else if(this instanceof Condition){
            varTally.putIfAbsent("cond",1);
            sb.append(String.format(Strings.repeat('\t',depth) + ":COND (c%d / %s\n", varTally.get("cond"), ((Condition) this).getName()));
            varTally.put("cond",varTally.get("cond")+1);
            depth++;
            for (String arg: ((Condition) this).getVarYield().keySet()){
                DRTVariable var = ((Condition) this).getVarYield().get(arg);
                String varName = var.getType() + String.valueOf(var.getIdx());
                if (visitedVar.contains(varName)){
                    sb.append(String.format("%s:VAR %s\n",Strings.repeat('\t',depth), varName));
                } else {
                    sb.append(String.format("%s:VAR (%s / %s)\n", Strings.repeat('\t', depth), varName, var.getType()));
                }
                visitedVar.add(varName);
            }
        }
        for (DRTElement child: children) {
            child.Graph2AMR(sb, depth + 1, varTally,visitedVar);
        }
        sb.append(Strings.repeat('\t',depth) + ")\n");
        return sb.toString().trim();
    }

    public ArraySet<String> checkConstituentParents(ArraySet<String> els){
        if (this instanceof Constituent) {
            if (this.getParent() instanceof Condition) {
                els.add("condition");
            } else if (this.getParent() instanceof Constituent) {
                els.add("constituent");
            } else if (this.getParent() instanceof DRS) {
                els.add("DRS");
            } else if (this.getParent() instanceof SDRS) {
                els.add("SDRS");
            } else if (this.getParent() instanceof Relation) {
                els.add("Relation");
            }
        }
        for(DRTElement child: this.getChildren()){
            this.checkConstituentParents(els);
        }

        return els;
    }


    public ArrayList<String> gatherGraphTriplets(ArrayList<String> collect){
        if (this instanceof Condition){
            String parentNode;
            if (this.getParent() instanceof DRS || this.getParent() instanceof SDRS){
                parentNode = "K:" + (this.getParent() instanceof DRS? ((DRS) this.getParent()).getIndexDRS():
                        ((SDRS) this.getParent()).getindexSDRS());
            } else {
                parentNode = this.getParent().toString();
            }

            StringBuilder childrenNodes = new StringBuilder();

            if (!this.getChildren().isEmpty()){
                for (int i=0; i<this.getChildren().size(); i++) {
                    DRTElement child = this.getChildren().get(i);
                    if (child instanceof DRS || child instanceof SDRS){
                        childrenNodes.append(String.format("c%d K:%s ", i, (child instanceof DRS? ((DRS) child).getIndexDRS():
                            ((SDRS) child).getindexSDRS())));
                    } else {
                        childrenNodes.append(String.format("c%d K:%s ", i, child.toString()));
                    }
                }
            } else {
                childrenNodes.append(((Condition) this).prettyVarYield() + " " +
                        ((Condition) this).getExtra().toString());
            }
            collect.add(String.format("%s -> %s -> %s",
                    parentNode,((Condition) this).getName(),childrenNodes.toString().trim()));


        } else if (this instanceof Relation){

            StringBuilder sb = new StringBuilder();

            String parentNode;
            if (this.getParent()!=null) {
                if (this.getParent() instanceof DRS || this.getParent() instanceof SDRS) {
                    parentNode = "K:" + (this.getParent() instanceof DRS? ((DRS) this.getParent()).getIndexDRS():
                            ((SDRS) this.getParent()).getindexSDRS());
                } else {
                    sb.append(this.getParent().toString());
                }
            } else {
                sb.append("K:0 ");
            }
            sb.append(String.format("-> %s ",(((Relation) this).getName())));

            for (int i=0; i<this.getChildren().size(); i++) {
                DRTElement child = this.getChildren().get(i);
                if (child instanceof DRS || child instanceof SDRS) {
                    sb.append(String.format("c%d K:%s ", i, (child instanceof DRS? ((DRS) child).getIndexDRS():
                            ((SDRS) child).getindexSDRS())));
                } else {
                    sb.append(String.format("c%d K:%s ", i, child.toString()));
                }
            }
            collect.add(sb.toString().trim());

        }

        for (DRTElement child: this.children){
            child.gatherGraphTriplets(collect);
        }
        return collect;
    }
}

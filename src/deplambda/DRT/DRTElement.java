package deplambda.DRT;

import edu.cornell.cs.nlp.spf.mr.lambda.Term;
import edu.stanford.nlp.util.ArraySet;
import joptsimple.internal.Strings;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

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

    public DRTElement addOrGetChild() {return null;}

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
        System.out.println(Strings.repeat('\t',depth) + this.toString());//+ " " + (this.parent!=null? this.parent.toString():"null"));
        for(DRTElement child:children){
            child.printChildren(depth+1);
        }
    }

    public void readjustRelations(List<String> allowedConsts){
        if (this instanceof SDRS){
//            take the relations
            ArraySet<Relation> rels = ((SDRS) this).getRelations();
            Map<String,Constituent> consts = ((SDRS) this).getConstituents();
//            if the relation is ! continuation ->
            for (Relation rel: rels){
                if (!rel.getName().equals("continuation")){
//                for each relation check the constituents it links
                    String arg0 = rel.getChildTags().get(0);
                    String arg1 = rel.getChildTags().get(1);
                    if (!allowedConsts.contains(arg0) || !allowedConsts.contains(arg1)){
//                find the two constituents linked
                        Constituent c1 = consts.get(arg0);
                        Constituent c2 = consts.get(arg1);
//                add them as child of the relation
                        rel.setParentChildRel(c1);
                        rel.setParentChildRel(c2);
                        this.setParentChildRel(rel);
//                remove the constituents as children of the SDRS
                        this.getChildren().remove(c1);
                        this.getChildren().remove(c2);
                    }
                }
            }
        }
        for (DRTElement el: this.getChildren()){
            el.readjustRelations(allowedConsts);
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

    public ArrayList<String> gatherGraphTriplets(ArrayList<String> collect){
        if (this instanceof Condition){
            String parentNode = this.getParent().toString();
            String childrenNodes = ((Condition) this).getVarYield().toString();
            collect.add(String.format("%s -> %s -> %s", parentNode,((Condition) this).getName(),childrenNodes));
        }
        for (DRTElement child: this.children){
            child.gatherGraphTriplets(collect);
        }
        return collect;
    }
}

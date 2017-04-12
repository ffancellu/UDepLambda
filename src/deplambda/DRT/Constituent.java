package deplambda.DRT;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * Created by ffancellu on 21/02/2017.
 */
public class Constituent extends DRTElement{

    private String label;

    public Constituent(){
        label = new String();
    }

    public String getLabel(){
        return label;
    }


    /**
     * Decide to which sentence index the constituent should be assigned
     * @return
     */
    public int assign2Sentence(){
        TreeSet<String> getIndicesConstituent = this.getIndicesYield(new TreeSet<>());
        if (getIndicesConstituent.isEmpty()){
            return -1;
        }
//        TODO: find the max indices and assign the constituent there
        HashMap<Integer,Integer> counts = new HashMap<>();
        for (String tokStr: getIndicesConstituent){
            int tokIdx = Integer.parseInt(tokStr.substring(1,2));
            counts.putIfAbsent(tokIdx,0);
            counts.put(tokIdx,counts.get(tokIdx)+1);
        }
        int max = Collections.max(counts.entrySet(), (entry1,entry2) -> entry1.getValue() - entry2.getValue()).getKey();
        return max;
    }

    @Override
    public void parseContent(Node constNode, Map<String,DRTVariable> allVars){
        label = constNode.getAttributes().getNamedItem("label").getNodeValue();
        NodeList nodes = constNode.getChildNodes();
        for (int i=0; i<nodes.getLength();i++){
            Node n = nodes.item(i);
            if (n.getNodeName().equals("drs")){
                DRS drs = new DRS();
                this.setParentChildRel(drs);
                drs.parseContent(n,allVars);
            } else if (n.getNodeName().equals("sdrs")){
                SDRS sdrs = new SDRS(false);
                this.setParentChildRel(sdrs);
                sdrs.parseContent(n,allVars);
            }
        }
    }

    @Override
    public String toString(){
        return String.format("Constituent::%s",label);
    }
}

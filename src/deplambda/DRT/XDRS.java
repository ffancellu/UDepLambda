package deplambda.DRT;


import org.apache.jena.atlas.lib.Tuple;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.*;

/**
 * Created by ffancellu on 25/02/2017.
 */
public class XDRS extends DRTElement{

    private SortedMap<String,TaggedToken> taggedTokens;

    public XDRS(){
        taggedTokens = new TreeMap<>();
    }

    public SortedMap<String,TaggedToken> getTaggedTokens(){return taggedTokens;}

    private Map<String,DRTVariable> gatherAllVariables(Node node) {
        Map<String,DRTVariable> XDRSVariables = new HashMap<>();
        NodeList allVars = ((Element) node).getElementsByTagName("dr");
        for (int i = 0;i<allVars.getLength();i++){
            DRTVariable var = new DRTVariable();
            var.parseContent(allVars.item(i));
            XDRSVariables.put(String.format("%s%d", var.getType(), var.getIdx()), var);
        }
        return XDRSVariables;
    }

    @Override
    public void parseContent(Node node){
        //first iterate through the entire document to fetch all the variables
        Map<String, DRTVariable> allVars = this.gatherAllVariables(node);
        for (int i =0; i< node.getChildNodes().getLength();i++){
            Node childNode = node.getChildNodes().item(i);
            if (childNode.getNodeName().equals("taggedtokens")){
                NodeList tokens = childNode.getChildNodes();
                for (int tokIdx = 0;tokIdx<tokens.getLength();tokIdx++){
                    if (tokens.item(tokIdx).getNodeName().equals("tagtoken")) {
                        Element tagNodes = (Element) tokens.item(tokIdx);
//                        assuming there is only one tag node underneath
                        Node tagNode = tagNodes.getElementsByTagName("tags").item(0);
                        TaggedToken token = new TaggedToken(tagNode);
                        taggedTokens.put(tokens.item(tokIdx).getAttributes().getNamedItem("xml:id").getNodeValue(),
                                token);
                    }
                }
            } else if (childNode.getNodeName().equals("sdrs")){
                SDRS sdrs = new SDRS(true);
                this.setParentChildRel(sdrs);
                sdrs.parseContent(childNode, allVars);
            }

        }
    }
}

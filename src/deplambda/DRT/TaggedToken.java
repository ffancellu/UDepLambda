package deplambda.DRT;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by ffancellu on 25/02/2017.
 */
public class TaggedToken {

    String token,POS,numex,animacy,timex,lemma,senseId,namex;
    int from,to,sense;

    public TaggedToken(Node tokenNode){
        NodeList tokenTagNode = tokenNode.getChildNodes();
        for (int i=0;i<tokenTagNode.getLength();i++){
            Node el = tokenTagNode.item(i);
            if (el.getNodeName().equals("tag")){
                String elName = el.getAttributes().getNamedItem("type").getNodeValue();
                switch (elName){
                    case "tok":
                        token = el.getTextContent();
                        break;
                    case "pos":
                        POS = el.getTextContent();
                        break;
                    case "numex":
                        numex = el.getTextContent();
                        break;
                    case "animacy":
                        animacy = el.getTextContent();
                        break;
                    case "timex":
                        timex = el.getTextContent();
                        break;
                    case "lemma":
                        lemma = el.getTextContent();
                        break;
                    case "senseId":
                        senseId = el.getTextContent();
                        break;
                    case "namex":
                        namex = el.getTextContent();
                        break;
                    case "from":
                        from = Integer.parseInt(el.getTextContent());
                        break;
                    case "to":
                        to = Integer.parseInt(el.getTextContent());
                        break;
                    case "sense":
                        sense = Integer.parseInt(el.getTextContent());
                        break;
                    default:
                        break;
                }
            }
        }

    }

    @Override
    public String toString(){
        return token;
    }
}

package deplambda.DRT;

import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import edu.cornell.cs.nlp.spf.mr.lambda.Term;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by ffancellu on 09/02/2017.
 */
public class DRTVariable extends DRTElement{

    private LogicalExpression term;
    private String type;
    private Integer idx;
    private String alignedTaggedToken = new String();

    public DRTVariable(){}

    public DRTVariable(Term term, String type, int idx){
        this.term = term;
        this.type = type;
        this.idx = idx;
    }

    public String getType(){
        return type;
    }

    public LogicalExpression getTerm(){
        return term;
    }

    public int getIdx() { return idx; }

    public void setIdx(Integer idx){ this.idx = idx;}

    @Override
    public boolean equals(Object o) {

        if (o==this){return true;}
        return this.term.equals(((DRTVariable) o).getTerm()) && this.type.equals(((DRTVariable) o).getType()) && this.idx.equals(((DRTVariable) o).getIdx());
    }

    //Idea from effective Java : Item 9
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + term.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + idx.hashCode();
        return result;
    }

    @Override
    public void parseContent(Node node){
        String name = node.getAttributes().getNamedItem("name").getNodeValue();
        type = name.substring(0,1);
        term = Term.read("$0:x");
        idx = Integer.parseInt(name.substring(1));
//        get all potential indices
        NodeList indexNodes = ((Element)node).getElementsByTagName("index");
        for (int i=0;i<indexNodes.getLength();i++){
            this.alignedTaggedToken = indexNodes.item(i).getTextContent();
            this.addToken(indexNodes.item(i).getTextContent());
        }

    }

    @Override
    public String toString(){
        return String.format("%s%d",type,idx);
    }

}

package deplambda.DRT;


import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ffancellu on 21/02/2017.
 */
public class Relation extends DRTElement{

    String name;
    HashMap<String,DRTElement> args;
    ArrayList<String> childTags = new ArrayList<>();

    public Relation(){
        this.name = new String();
        this.args = new HashMap<>();
    }

    public Relation(String name, HashMap<String, DRTElement> args){
        this.name = name;
        this.args = args;
    }

    public void setName(String name){
        this.name = name;
    }
    public String getName() { return this.name;}
    public HashMap<String,DRTElement> getArgs(){ return this.args;}
    public ArrayList<String> getChildTags(){return this.childTags;}


    public void addChildrenConstituents(){
        this.args.keySet().forEach(key -> this.setParentChildRel(this.args.get(key)));
    }

    @Override
    public void parseContent(Node node){
        name = node.getAttributes().getNamedItem("sym").getNodeValue();
        childTags.add(node.getAttributes().getNamedItem("arg1").getNodeValue());
        childTags.add(node.getAttributes().getNamedItem("arg2").getNodeValue());
        NodeList indices = ((Element) node).getElementsByTagName("index");
        for (int i =0;i<indices.getLength();i++){
            this.addToken(indices.item(i).getTextContent());
        }
    }

    @Override
    public String toString(){
        return this.name;
    }


}

package deplambda.DRGLambda;

import deplambda.DRG.*;
import deplambda.DRT.*;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SetHypergraph;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;


/**
 * Created by ffancellu on 21/06/2017.
 */
public class GraphProcessor {

    public static void createGraphs(ArrayList<Triple<String, String, ArrayList<Pair>>> processedRoots){
        for (Triple t: processedRoots){
            for (Pair pair : (ArrayList<Pair>) t.third()) {
                DRTElement drt = ((DRTElement) pair.second());
                Hypergraph<Vertex,Hyperedge> hypergraph = new SetHypergraph<>();
                createGraph(drt, hypergraph);
                for (Hyperedge e:hypergraph.getEdges()){
                    System.out.println(e.toString());
                }
                System.out.println();
            }
        }
    }

    private static void createGraph(DRTElement drt, Hypergraph G) {
        List<Vertex> vertices = new ArrayList<>();
        Vertex k0, k1, k2, c0, c1;
        DRTVariable v0, v1;
        if (drt instanceof Condition){
            Condition cond = (Condition) drt;
            switch(cond.getType()){
                case "unaryPred":
                    v0 = cond.getVarYield().get("arg0");
                    k0 = new Vertex(drt.getParent().toString());
                    c0 = new Vertex(v0.toString());
                    vertices.add(k0);
                    vertices.add(c0);
                    G.addVertex(k0);
                    G.addVertex(c0);
                    UnaryPredicate up = new UnaryPredicate(k0,c0);
                    up.setType(v0.getType());
                    up.setAlignedToken(cond.getAlignedTaggedToken());
                    G.addEdge(up,vertices);
                    break;
                case "binaryPred":
                    v0 = cond.getVarYield().get("arg1");
                    v1 = cond.getVarYield().get("arg2");
                    k0 = new Vertex(drt.getParent().toString());
                    c0 = new Vertex(v0.toString());
                    c1 = new Vertex(v1.toString());
                    vertices.add(k0);
                    vertices.add(c0);
                    vertices.add(c1);
                    G.addVertex(k0);
                    G.addVertex(c0);
                    G.addVertex(c1);
                    BinaryPredicate bp = new BinaryPredicate(k0,c0,c1);
                    bp.setType(v0.getType()+'_'+v1.getType());
                    bp.setAlignedToken(cond.getAlignedTaggedToken());
                    G.addEdge(bp,vertices);
                    break;
                case "unaryOperator":
                    k0 = new Vertex(drt.getParent().toString());
                    k1 = new Vertex(drt.getChildren().get(0).toString());
                    vertices.add(k0);
                    vertices.add(k1);
                    G.addVertex(k0);
                    G.addVertex(k1);
                    UnaryOperator uo = new UnaryOperator(k0,k1);
                    uo.setType("o");
                    uo.setAlignedToken(cond.getAlignedTaggedToken());
                    G.addEdge(uo,vertices);
                    break;
                case "binaryOperator":
                    k0 = new Vertex(drt.getParent().toString());
                    k1 = new Vertex(drt.getChildren().get(0).toString());
                    k2 = new Vertex(drt.getChildren().get(1).toString());
                    vertices.add(k0);
                    vertices.add(k1);
                    vertices.add(k2);
                    G.addVertex(k0);
                    G.addVertex(k1);
                    G.addVertex(k2);
                    deplambda.DRG.BinaryOperator bo = new deplambda.DRG.BinaryOperator(k0,k1,k2);
                    bo.setType("O");
                    bo.setAlignedToken(cond.getAlignedTaggedToken());
                    G.addEdge(bo,vertices);
                    break;
            }
        } else if (drt instanceof Relation){
            if (drt.getParent()==null){
                k0 = new Vertex("K0");
            } else {
                k0 = new Vertex(drt.getParent().toString());
            }
            k1 = new Vertex(drt.getChildren().get(0).toString());
            k2 = new Vertex(drt.getChildren().get(1).toString());
            vertices.add(k0);
            vertices.add(k1);
            vertices.add(k2);
            G.addVertex(k0);
            G.addVertex(k1);
            G.addVertex(k2);
            Connector c = new Connector(k0,k1,k2);
            c.setType("C");
            c.setAlignedToken(((Relation) drt).getAlignedTaggedToken());
            G.addEdge(c,vertices);

        }
        for (DRTElement child: drt.getChildren()){
            createGraph(child,G);
        }
    }



}

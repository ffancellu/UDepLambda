package deplambda.DRG;

/**
 * Created by ffancellu on 22/06/2017.
 */
public class UnaryPredicate extends Hyperedge{

    private Vertex K;
    private Vertex child;

    public UnaryPredicate(Vertex K, Vertex child){
        this.K = K;
        this.child = child;
    }

    @Override
    public String toString(){
        return this.K.getLabel() + " " + this.getType() + " " + this.child.getLabel() + " :: " + this.getAlignedToken();
    }
}

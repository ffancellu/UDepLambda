package deplambda.DRG;

/**
 * Created by ffancellu on 22/06/2017.
 */
public class UnaryOperator extends Hyperedge {

    private Vertex k0,k1;

    public UnaryOperator(Vertex k0, Vertex k1){
        this.k0 = k0;
        this.k1 = k1;
    }

    @Override
    public String toString(){
        return this.k0.getLabel() + " " + this.k1.getLabel() + " :: " + this.getAlignedToken();
    }
}

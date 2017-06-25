package deplambda.DRG;

/**
 * Created by ffancellu on 22/06/2017.
 */
public class BinaryPredicate extends Hyperedge {

    private Vertex K;
    private Vertex external,internal;

    public BinaryPredicate(Vertex K, Vertex e, Vertex i){
        this.K = K;
        this.external = e;
        this.internal = i;
    }

    @Override
    public String toString(){
        return String.format("%s %s [%s,%s] :: %s",this.K.getLabel(),
                                                    this.getType(),
                                                    this.external.getLabel(),
                                                    this.internal.getLabel(),
                                                    this.getAlignedToken());
    }
}

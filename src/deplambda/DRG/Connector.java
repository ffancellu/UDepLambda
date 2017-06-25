package deplambda.DRG;

/**
 * Created by ffancellu on 22/06/2017.
 */
public class Connector extends Hyperedge{

    private Vertex K0;
    private Vertex K1,K2;

    public Connector(Vertex K0, Vertex K1, Vertex K2){
        this.K0 = K0;
        this.K1 = K1;
        this.K2 = K2;
    }

    @Override
    public String toString(){
        return String.format("%s %s [%s %s] :: %s",this.K0.getLabel(),
                                                    this.getType(),
                                                    this.K1.getLabel(),
                                                    this.K2.getLabel(),
                                                    this.getAlignedToken());
    }
}

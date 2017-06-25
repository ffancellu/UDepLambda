package deplambda.DRG;

/**
 * Created by ffancellu on 22/06/2017.
 */
public class Hyperedge {

    private String alignedToken;
    private String type;

    public Hyperedge(){}

    public String getType(){return this.type;}
    public String getAlignedToken(){return this.alignedToken;}

    public void setAlignedToken(String token){
        this.alignedToken = token;
    }
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString(){return "";}
}

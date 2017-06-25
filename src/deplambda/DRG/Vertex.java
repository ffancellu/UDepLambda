package deplambda.DRG;

import deplambda.DRT.DRTVariable;

/**
 * Created by ffancellu on 22/06/2017.
 */
public class Vertex {

    private String label;

    public Vertex(String label){
        this.label=label;
    }

    public String getLabel(){return this.label;}

    @Override
    public boolean equals(Object o) {

        if (o==this){return true;}
        return this.label.equals(((Vertex) o).getLabel());
    }

    //Idea from effective Java : Item 9
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + label.hashCode();
        return result;
    }
}

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class Main {
    public static void main(String[] args){
        Model model = RDFDataMgr.loadModel("data/ontology_restaurant1_rdf.owl");
        //Dataset dataset = RDFDataMgr.loadDataset("data/restaurant1.rdf") ;
        System.out.println(model.size());
    }
}

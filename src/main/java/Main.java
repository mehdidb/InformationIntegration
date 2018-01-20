import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class Main {
    public static void main(String[] args){
        System.out.println("The model is :");
        Model model = RDFDataMgr.loadModel("data/ontology_restaurant1_rdf.owl");
        RDFDataMgr.read(model, "data/restaurant1.rdf");
        RDFDataMgr.write(System.out, model, Lang.RDFXML) ;

        //System.out.println("The model from dataset is :");
        //Dataset data = RDFDataMgr.loadDataset("data/restaurant1.rdf");
        //RDFDataMgr.write(System.out, data.getDefaultModel(), Lang.RDFXML) ;


        //model1.
        //RDFDataMgr.read(model1, "data/restaurant1.rdf") ;
        //RDFList liste = model1.createList();
        //System.out.println(liste.size());
        //for (RDFNode node : liste.asJavaList()) {
        //    System.out.println("1");
        //}
        //Model model2 = RDFDataMgr.loadModel("data/ontology_restaurant2_rdf.owl");


        //Dataset dataset = RDFDataMgr.loadDataset() ;
        // RDFDataMgr.read(model, "data/restaurant2.rdf");
        //dataset.setDefaultModel(model1);
        //System.out.println(model1.isIsomorphicWith(dataset.getDefaultModel()));
        //System.out.println(model1.size());
        //System.out.println(model2.size());
    }
}

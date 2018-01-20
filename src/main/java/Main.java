import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.VCARD;

public class Main {
    public static void main(String[] args){
        Model model1 = RDFDataMgr.loadModel("data/ontology_restaurant1_rdf.owl");
        RDFDataMgr.read(model1, "data/restaurant1.rdf");

        Model model2 = RDFDataMgr.loadModel("data/ontology_restaurant2_rdf.owl");
        RDFDataMgr.read(model1, "data/restaurant2.rdf");

        Model mapping = RDFDataMgr.loadModel("data/mapping_restaurant.rdf");
        //RDFDataMgr.write(System.out, model, Lang.RDFXML) ;

        ResIterator iter = model1.listSubjects();
        while (iter.hasNext()) {
            Resource r = iter.nextResource();
            if (r.getLocalName() == null) {
                continue;
            }
            if (r.getLocalName().equals("restaurant1-Restaurant0")) { // r.getLocalName().equals("restaurant1-Restaurant0")
                //System.out.println(r);
                StmtIterator properties = r.listProperties();

                while (properties.hasNext()) {
                    Statement p = properties.nextStatement();
                    if (!p.getObject().toString().contains("Address")) {
                        //System.out.println(p.getSubject() + " " + p.getPredicate() + " " + p.getObject());
                    }

                    if (p.getObject().toString().contains("Address")) {
                        StmtIterator address = model1.getResource(p.getObject().toString()).listProperties();
                        while (address.hasNext()) {
                            Statement a = address.nextStatement();
                            //System.out.println(a.getSubject() + " " + a.getPredicate() + " " + a.getObject());
                        }
                    }
                }
                break;
            }

        }

        /**
        System.out.println("DEBUG : " );
        Property it2 = mapping.getProperty("http://www.okkam.org/ontology_restaurant1.owl#street");
        System.out.println(it2);
        StmtIterator it = it2.listProperties();
        while (it.hasNext()) {
            Statement a = it.nextStatement();
            System.out.println(a.getSubject() + " " + a.getPredicate() + " " + a.getObject());
        }
         */
        /**
        NodeIterator node = mapping.listObjects();
        while (node.hasNext()) {
            Resource r = node.next().asResource();
            StmtIterator properties = r.listProperties();

            while (properties.hasNext()) {
                Statement p = properties.nextStatement();
                System.out.println(p.getSubject() + " " + p.getPredicate() + " " + p.getObject());
            }
        }
         */

        ResIterator subject = mapping.listSubjects();
        while (subject.hasNext()) {
            Resource r = subject.next();
            StmtIterator properties = r.listProperties();

            while (properties.hasNext()) {
                Statement p = properties.nextStatement();
                System.out.println(p.getSubject() + " " + p.getPredicate() + " " + p.getObject());
            }
        }
    }
}

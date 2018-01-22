import org.apache.jena.ontology.*;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.VCARD;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;

public class Main {
    public static void main(String[] args){
        Model model1 = RDFDataMgr.loadModel("data/ontology_restaurant1_rdf.owl");
        RDFDataMgr.read(model1, "data/restaurant1.rdf");

        Model model2 = RDFDataMgr.loadModel("data/ontology_restaurant2_rdf.owl");
        //RDFDataMgr.read(model2, "data/restaurant2.rdf");

        Model mapping = RDFDataMgr.loadModel("data/mapping_restaurant.rdf");
        //RDFDataMgr.write(System.out, model, Lang.RDFXML) ;

        OntModel m = getOntologyModel("data/ontology_restaurant2_rdf.owl");

        for (OntClass i : m.listClasses().toList()) {
            //System.out.println(i);
        }

        ResIterator iter = model1.listSubjects();
        while (iter.hasNext()) {
            Resource r = iter.nextResource();
            if (r.getLocalName() == null) {
                continue;
            }
            if (r.getLocalName().equals("restaurant2-Restaurant0")) { // r.getLocalName().equals("restaurant1-Restaurant0")
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
        HashMap<String, HashSet<Statement>> hm = new HashMap<String, HashSet<Statement>>();
        while (subject.hasNext()) {
            Resource r = subject.next();
            StmtIterator properties = r.listProperties();

            if (!hm.containsKey(r.toString())) {
                hm.put(r.toString(), new HashSet<Statement>());
            }

            while (properties.hasNext()) {
                Statement p = properties.nextStatement();
                if (!p.getPredicate().toString().contains("mapTo")) {
                    hm.get(r.toString()).add(p);
                }
            }
        }

        subject = mapping.listSubjects();
        HashMap<String, String> mappingHM = new HashMap<String, String>();
        while (subject.hasNext()) {
            Resource r = subject.next();
            StmtIterator properties = r.listProperties();

            while (properties.hasNext()) {
                Statement p = properties.nextStatement();


                if (p.getPredicate().toString().contains("mapTo")) {


                    HashSet<Statement> hsS1 = hm.get(p.getSubject().toString());
                    HashSet<Statement> hsS2 = hm.get(p.getObject().toString());
                    if (hsS1.size() == 1 && hsS2.size() == 1) {
                        mappingHM.put(hsS1.iterator().next().getObject().toString(), hsS2.iterator().next().getObject().toString());
                    }
                    /**
                    for (Statement st : hsS) {
                        System.out.print(st.getObject() + " ");
                    }


                    for (Statement st : hsSt) {
                        System.out.print(" Mapped to : " + st.getObject());
                    }
                    System.out.println();
                     */
                }
                //System.out.println(p.getSubject() + " -> " + p.getPredicate() + " -> " + p.getObject());
            }
            //System.out.println("-------");
        }
        /**
        for (String key : mappingHM.keySet()) {
            //System.out.println(key + " -> " + mappingHM.get(key));
        }
         */
        /**
        //System.out.println("======================================");
        ResIterator it = model1.listSubjects();
        while (it.hasNext()) {
            Resource r = it.nextResource();
            //System.out.println(r);
            StmtIterator properties = r.listProperties();
            while (properties.hasNext()) {
                Statement p = properties.nextStatement();
                Resource sub = p.getSubject();
                Property prd = p.getPredicate();
                RDFNode obj = p.getObject();
                if (mappingHM.containsKey(p.getPredicate().toString())) {
                    prd = ResourceFactory.createProperty(mappingHM.get(p.getPredicate().toString()));
                    Statement s = ResourceFactory.createStatement(sub, prd, obj);
                    model1.remove(p);
                    model2.add(s);
                    properties = r.listProperties();
                }

                if (p.getPredicate().toString().contains("is_in_city")) {
                    obj = model1.createTypedLiteral(model1.getResource(p.getObject().toString()).listProperties().nextStatement().getObject());
                    Resource rs1 = model1.getResource(p.getObject().toString());
                    StmtIterator prop = rs1.listProperties();
                    while (prop.hasNext()) {
                        Statement a = prop.nextStatement();
                        model1.remove(a);
                        prop = rs1.listProperties();
                    }

                    Statement s = ResourceFactory.createStatement(sub, prd, obj);
                    model1.remove(p);
                    model2.add(s);
                    properties = r.listProperties();
                }


            }
        }
        */
        int id = 0;
        ResIterator it = model1.listSubjects();
        while (it.hasNext()) {
            Resource r = it.nextResource();
            if (r.toString().contains("City"))
                continue;
            StmtIterator properties = r.listProperties();
            while (properties.hasNext()) {
                Statement p = properties.nextStatement();
                //System.out.println(p.getSubject() + " -> " + p.getPredicate() + " -> " + p.getObject());
                Resource sub = p.getSubject();
                Property prd = p.getPredicate();
                RDFNode obj = p.getObject();
                if (mappingHM.containsKey(p.getPredicate().toString())) {
                    prd = ResourceFactory.createProperty(mappingHM.get(p.getPredicate().toString()));
                    Statement s = ResourceFactory.createStatement(sub, prd, obj);
                    model1 = model1.remove(p);
                    model1 = model1.add(s);
                    properties = r.listProperties();
                }

                if (p.getPredicate().toString().contains("is_in_city")) {
                    prd = ResourceFactory.createProperty("http://www.okkam.org/ontology_restaurant2.owl#city");
                    Resource rs1 = model1.getResource(p.getObject().toString());
                    StmtIterator prop = rs1.listProperties();
                    while (prop.hasNext()) {
                        Statement a = prop.nextStatement();
                        if (a.getPredicate().toString().contains("name")) {
                            obj = ResourceFactory.createStringLiteral(model1.getResource(a.getObject().toString()).toString());//model1.createTypedLiteral();
                        }
                        model1 = model1.remove(a);
                        prop = rs1.listProperties();
                    }

                    Statement s = ResourceFactory.createStatement(sub, prd, obj);
                    model1 = model1.remove(p);
                    model1 = model1.add(s);
                    properties = r.listProperties();
                }

                if (p.getPredicate().toString().contains("category") && !p.getPredicate().toString().contains("has_category")) {

                    Individual cat = m.createIndividual("http://www.okkam.org/ontology_restaurant2.owl#category" + id++, m.getOntClass("http://www.okkam.org/ontology_restaurant2.owl#Category"));
                    DatatypeProperty name = m.createDatatypeProperty("http://www.okkam.org/ontology_restaurant2.owl#name");
                    cat.addProperty(name, m.createLiteral(p.getObject().toString()));
                    prd = ResourceFactory.createProperty("http://www.okkam.org/ontology_restaurant2.owl#has_category");
                    obj = ResourceFactory.createProperty(cat.getURI());//model1.createTypedLiteral();

                    Statement s = ResourceFactory.createStatement(sub, prd, obj);
                    model1 = model1.remove(p);
                    model1 = model1.add(s);
                    properties = r.listProperties();
                }
            }

            properties = r.listProperties();
            while (properties.hasNext()) {
                Statement p = properties.nextStatement();
                model2.add(p);
                m.add(p);
            }
        }

        //RDFDataMgr.write(System.out, m, Lang.RDFXML);
    }

    public static OntModel getOntologyModel(String ontoFile)
    {
        OntModel ontoModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        try
        {
            InputStream in = FileManager.get().open(ontoFile);
            try
            {
                ontoModel.read(in, null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            //System.out.println("Ontology " + ontoFile + " loaded.");
        }
        catch (JenaException je)
        {
            System.err.println("ERROR" + je.getMessage());
            je.printStackTrace();
            System.exit(0);
        }
        return ontoModel;
    }
}

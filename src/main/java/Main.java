import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.FileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;

public class Main {
    private static String ONT_1_LINK;
    private final static String ONT_1_OWL = "data/ontology_restaurant1_rdf.owl";
    private final static String ONT_2_OWL = "data/ontology_restaurant2_rdf.owl";
    private final static String ONT_1_RDF = "data/restaurant1.rdf";
    private final static String ONT_2_RDF = "data/restaurant2.rdf";
    private final static String MAPPING_FILE = "data/mapping_restaurant.rdf";
    private final static String OUTPUT_RDF = "data/restaurant1_out.rdf";

    private static String getOntlogyLink() {
        OntModel m = getOntologyModel(ONT_1_OWL);
        for (OntClass i : m.listClasses().toList()) {
            if (ONT_1_LINK == null) {
                return i.toString().split("#")[0];
            }
        }

        return null;
    }

    private static void debugModel(Model m) {
        // For science
        ResIterator it = m.listSubjects();
        while (it.hasNext()) {
            Resource r = it.nextResource();
            if (r.getLocalName() == null) {
                continue;
            }
            System.out.println(r);
            if (r.getLocalName().equals("restaurant1-Restaurant0")) {
                System.out.println(r);
                StmtIterator properties = r.listProperties();

                while (properties.hasNext()) {
                    Statement p = properties.nextStatement();
                    if (!p.getObject().toString().contains("Address")) {
                        System.out.println(p.getSubject() + " " + p.getPredicate() + " " + p.getObject());
                    }

                    if (p.getObject().toString().contains("Address")) {
                        StmtIterator address = m.getResource(p.getObject().toString()).listProperties();
                        while (address.hasNext()) {
                            Statement a = address.nextStatement();
                            System.out.println(a.getSubject() + " " + a.getPredicate() + " " + a.getObject());
                        }
                    }
                }
                break;
            }
        }
    }

    private static void printModel(Model m) {
        ResIterator it = m.listSubjects();
        while (it.hasNext()) {
            Resource r = it.nextResource();
            StmtIterator properties = r.listProperties();

            while (properties.hasNext()) {
                Statement p = properties.nextStatement();
                System.out.println(p.getSubject() + " " + p.getPredicate() + " " + p.getObject());
                return;
            }
        }
    }

    private static void printClasses(OntModel m) {
        for (OntClass i : m.listClasses().toList()) {
            System.out.println(i);
        }
    }

    private static void outputModel(OntModel m) {
        FileOutputStream fop = null;
        File file;
        try {
            file = new File(OUTPUT_RDF);
            fop = new FileOutputStream(file);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // get the content in bytes
            RDFDataMgr.write(fop, m, Lang.RDFXML);
            fop.flush();
            fop.close();

            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static HashSet<Pair<HashSet<Statement>, HashSet<Statement>>> generateSets(Model map) {
        HashSet<Pair<HashSet<Statement>, HashSet<Statement>>> ret = new HashSet<Pair<HashSet<Statement>, HashSet<Statement>>>();

        ResIterator subject = map.listSubjects();
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

        subject = map.listSubjects();
        while (subject.hasNext()) {
            Resource r = subject.next();
            StmtIterator properties = r.listProperties();

            while (properties.hasNext()) {
                Statement p = properties.nextStatement();

                if (p.getPredicate().toString().contains("mapTo")) {
                    HashSet<Statement> hs1 = hm.get(p.getSubject().toString());
                    HashSet<Statement> hs2 = hm.get(p.getObject().toString());
                    if (hs1.iterator().next().getPredicate().toString().contains(ONT_1_LINK)) {
                        ret.add(new ImmutablePair<HashSet<Statement>, HashSet<Statement>>(hs1, hs2));
                    } else {
                        ret.add(new ImmutablePair<HashSet<Statement>, HashSet<Statement>>(hs2, hs1));
                    }

                }
            }
        }

        return ret;
    }

    public static void main(String[] args){
        /**
         * Read the ontology and the RDF graph from restaurant1
         */
        Model model1 = RDFDataMgr.loadModel(ONT_1_OWL);
        RDFDataMgr.read(model1, ONT_1_RDF);
        //printModel(model1);

        /**
         * Read the ontology of restaurant2
         */
        Model model2 = RDFDataMgr.loadModel(ONT_2_OWL);

        ONT_1_LINK = getOntlogyLink();

        /**
         * Read the Mapping file and generate sets
         * Basically :
         * Element of Restaurant1 -> Element of Restaurant2
         */
        Model mapping = RDFDataMgr.loadModel(MAPPING_FILE);
        HashSet<Pair<HashSet<Statement>, HashSet<Statement>>> set = generateSets(mapping);

        OntModel m1 = getOntologyModel(ONT_1_OWL);
        OntModel m = getOntologyModel(ONT_2_OWL);



        for (Pair<HashSet<Statement>, HashSet<Statement>> i : set) {

            for (Statement st : i.getLeft()) {
                System.out.print("[" + st.getPredicate() + " " + st.getObject() + "] ");
            }

            System.out.print(" ----> ");
            for (Statement st : i.getRight()) {
                System.out.print("[" + st.getPredicate() + " " + st.getObject() + "] ");
            }

            System.out.println();
        }

        /**
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
         */
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

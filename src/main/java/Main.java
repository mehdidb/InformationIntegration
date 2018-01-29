import org.apache.commons.lang3.StringUtils;
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
    private static String ONT_2_LINK;
    private final static String ONT_1_OWL = "data/ontology_restaurant1_rdf.owl";
    private final static String ONT_2_OWL = "data/ontology_restaurant2_rdf.owl";
    private final static String ONT_1_RDF = "data/restaurant1.rdf";
    private final static String ONT_2_RDF = "data/restaurant2.rdf";
    private final static String MAPPING_FILE = "data/mapping_restaurant.rdf";
    private final static String OUTPUT_RDF = "data/restaurant1_out.rdf";
    private static HashSet<String> entities11;
    private static HashSet<String> entities1N;
    private static HashSet<String> entitiesN1;
    private static HashSet<String> avoided;

    /**
     * Permet de retourner le lien de la première ontologie
     *
     * @return
     */
    private static String getOntlogyLink1() {
        OntModel m = getOntologyModel(ONT_1_OWL);
        for (OntClass i : m.listClasses().toList()) {
            if (ONT_1_LINK == null) {
                return i.toString().split("#")[0];
            }
        }

        return null;
    }

    /**
     * Permet de retourner le lien de la deuxième ontologie
     * @return
     */
    private static String getOntlogyLink2() {
        OntModel m = getOntologyModel(ONT_2_OWL);
        for (OntClass i : m.listClasses().toList()) {
            if (ONT_2_LINK == null) {
                return i.toString().split("#")[0];
            }
        }

        return null;
    }
    /*
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
            }
        }
    }
    */
    /**
     * private static void printClasses(OntModel m) {
     * for (OntClass i : m.listClasses().toList()) {
     * System.out.println(i);
     * ExtendedIterator<OntProperty> list = i.listDeclaredProperties();
     * while (list.hasNext()) {
     * Property p = list.next();
     * System.out.println("Property : " + p);
     * }
     * }
     * }
     */

    /** Permet de générer le flux résultat
     * Dans notre cas, il est redirigé vers un fichier dans le dossier data
     * @param m
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void outputModel(OntModel m) {
        FileOutputStream fop = null;
        File file;
        try {
            file = new File(OUTPUT_RDF);
            fop = new FileOutputStream(file);

            // if file doesnt exists, then create it
            if (!file.exists()) file.createNewFile();

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

    /** Permet de générer l'ensemble des paires décrivant les mappaages.
     *
     * L'élément gauche de chaque paire représente les triplets relatifs à la première ontologie
     * L'élement droit représente les triplets relatif à la deuxième ontologie
     * L'ensemble sera utilisé pour déterminer la nature du mappage à effectuer ainsi que les propriétés à mapper
     * @param map
     * @return
     */
    private static HashSet<Pair<HashSet<Statement>, HashSet<Statement>>> generateSets(Model map) {
        HashSet<Pair<HashSet<Statement>, HashSet<Statement>>> ret = new HashSet<>();

        ResIterator subject = map.listSubjects();
        HashMap<String, HashSet<Statement>> hm = new HashMap<>();
        while (subject.hasNext()) {
            Resource r = subject.next();
            StmtIterator properties = r.listProperties();

            if (!hm.containsKey(r.toString())) {
                hm.put(r.toString(), new HashSet<>());
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
                    if (!hs1.iterator().next().getPredicate().toString().contains(ONT_1_LINK)) {
                        ret.add(new ImmutablePair<>(hs1, hs2));
                    } else {
                        ret.add(new ImmutablePair<>(hs2, hs1));
                    }

                }
            }
        }

        return ret;
    }
    /*
    private static void printSets(HashSet<Pair<HashSet<Statement>, HashSet<Statement>>> set) {
        for (Pair<HashSet<Statement>, HashSet<Statement>> i : set) {
            System.out.print("[ ");
            for (Statement st : i.getLeft())
                System.out.print(st.getPredicate() + "," + st.getObject() + " ");
            //System.out.print(i.getLeft());
            System.out.print("] ----> [ ");
            for (Statement st : i.getRight())
                System.out.print(st.getPredicate() + "," + st.getObject() + " ");
            //System.out.print(i.getRight());
            System.out.println("]");
        }
    }


        private static HashSet<String> findMatch(HashSet<Pair<HashSet<Statement>, HashSet<Statement>>> s, HashSet<Statement> q) {
            //System.out.println("Entering");
            HashSet<String> qHS = new HashSet<>();
            for (Statement v : q) {
                if ((v.getPredicate().toString()).contains(ONT_1_LINK)) {
                    //System.out.println(new String((v.getPredicate()).toString()));
                    qHS.add(new String((v.getPredicate()).toString()));
                }
            }

            //System.out.println("DEBUG qHS=" + qHS);

            for (Pair<HashSet<Statement>, HashSet<Statement>> p : s) {
                HashSet<String> lHS = new HashSet<>();

                for (Statement v : p.getLeft()) {
                    if ((v.getObject().toString()).contains(ONT_1_LINK)) {
                        //System.out.println(new String((v.getObject()).toString()));
                        lHS.add(new String((v.getObject()).toString()));
                    }
                }

                //System.out.println("DEBUG lHS=" + lHS);

                if (lHS.equals(qHS)) {
                    HashSet<String> ret = new HashSet<>();
                    for (Statement v : p.getRight()) {
                        if ((v.getObject().toString()).contains(ONT_2_LINK)) {
                            //System.out.println(v.getObject().toString());
                            ret.add(new String((v.getObject()).toString()));
                        }
                    }
                    //System.out.println("Exiting");
                    return ret;
                }

            }

            return null;
        }
        */

    /** Permet des générer les propriétés à mapper en situation 1 -> 1 en isolant les URIs des prédicats
     *
     * @param s
     * @return
     */
    private static HashSet<String> generateEntities11(HashSet<Pair<HashSet<Statement>, HashSet<Statement>>> s) {
        HashSet<String> ret = new HashSet<>();
        for (Pair<HashSet<Statement>, HashSet<Statement>> p : s) {
            HashSet<String> hs1 = new HashSet<>();
            HashSet<String> hs2 = new HashSet<>();

            p.getLeft().forEach(st -> hs1.add((st.getPredicate()).toString()));
            p.getRight().forEach(st -> hs2.add((st.getPredicate()).toString()));

            if (hs1.size() == 1 && hs2.size() == 1) {
                ret.add(p.getLeft().iterator().next().getObject().toString());
            }
        }

        return ret;
    }

    /** Permet des générer les propriétés à mapper en situation 1 -> N en isolant les URIs des prédicats
     *
     * @param s
     * @return
     */
    private static HashSet<String> generateEntities1N(HashSet<Pair<HashSet<Statement>, HashSet<Statement>>> s) {
        HashSet<String> ret = new HashSet<>();
        for (Pair<HashSet<Statement>, HashSet<Statement>> p : s) {
            HashSet<String> hs1 = new HashSet<>();
            HashSet<String> hs2 = new HashSet<>();

            p.getLeft().forEach(st -> hs1.add((st.getPredicate()).toString()));
            p.getRight().forEach(st -> hs2.add((st.getPredicate()).toString()));

            if (hs1.size() == 1 && hs2.size() > 1) {
                ret.add(p.getLeft().iterator().next().getObject().toString());
            }
        }

        return ret;
    }

    /** Permet des générer les propriétés à mapper en situation N -> 1 en isolant les URIs des prédicats
     *
     * @param s
     * @return
     */
    private static HashSet<String> generateEntitiesN1(HashSet<Pair<HashSet<Statement>, HashSet<Statement>>> s) {
        HashSet<String> ret = new HashSet<>();
        for (Pair<HashSet<Statement>, HashSet<Statement>> p : s) {
            HashSet<String> hs1 = new HashSet<>();
            HashSet<String> hs2 = new HashSet<>();

            p.getLeft().forEach(st -> hs1.add((st.getPredicate()).toString()));
            p.getRight().forEach(st -> hs2.add((st.getPredicate()).toString()));

            if (hs1.size() > 1 && hs2.size() == 1) {
                for (Statement st : p.getLeft()) {
                    if (st.getPredicate().toString().contains("#entity1")) {
                        ret.add(st.getObject().toString());
                    }
                }
                //ret.add(p.getLeft().iterator().next().getObject().toString()); // hs1.size() > 1
            }
        }

        return ret;
    }

    /** Permet d'avoir la paire représentant le prédicat hs (une URI) dans l'ensemble de mappage du sujet de la ressource.
     *
     * @param set
     * @param hs
     * @return
     */
    private static Pair<HashSet<Statement>, HashSet<Statement>> getMappedURI(HashSet<Pair<HashSet<Statement>, HashSet<Statement>>> set, String hs) {
        for (Pair<HashSet<Statement>, HashSet<Statement>> p : set) {
            for (Statement i : p.getLeft()) {
                if (i.getPredicate().toString().contains("entity1") && i.getObject().toString().equals(hs)) {
                    return p;
                }
            }
        }

        return null;
    }

    /**
     * Cette fonction permet de déterminer les classes d'invdidus que l'on va ignoner. Il s'agit de celles vers lesquelles
     * pointent les Object Properties des mappages N->1 (Range), parce qu'ils ne sont pas mappables (en effet, ces classes
     * n'existent pas dans l'autre ontologie). Plus tard, on aura juste besoin de vérifier le type des individus.
     * @return
     */
    private static HashSet<String> avoidedClass() {
        HashSet<String> ret = new HashSet<String>();
        OntModel m = getOntologyModel(ONT_1_OWL);
        for (String i : entitiesN1) {
            for (OntResource r : m.getOntProperty(i).listRange().toList()) {
                ret.add(r.toString());
            }
        }

        return ret;
    }

    public static void main(String[] args) {
        /*
         * Read the ontology and the RDF graph from restaurant1
         */
        Model model1 = RDFDataMgr.loadModel(ONT_1_OWL);
        RDFDataMgr.read(model1, ONT_1_RDF);
        //printModel(model1);

        /*
         * Read the ontology of restaurant2
         */
        //Model model2 = RDFDataMgr.loadModel(ONT_2_OWL);
        //printModel(model2);

        /*
         * Get the Ontology Link to
         */
        ONT_1_LINK = getOntlogyLink1();
        //System.out.println(ONT_1_LINK);
        ONT_2_LINK = getOntlogyLink2();
        //System.out.println(ONT_2_LINK);

        /*
         * Read the Mapping file and generate sets
         * Basically :
         * Element of Restaurant1 -> Element of Restaurant2
         */
        Model mapping = RDFDataMgr.loadModel(MAPPING_FILE);
        HashSet<Pair<HashSet<Statement>, HashSet<Statement>>> set = generateSets(mapping);
        //printModel(mapping);
        //printSets(set);

        /*
         * Generate all 1..1 entities
         */
        entities11 = generateEntities11(set);

        /*
         * Generate all 1..N entities
         */
        entities1N = generateEntities1N(set);

        /*
         Generate all N..1 entities
         */
        entitiesN1 = generateEntitiesN1(set);
        //System.out.println(entitiesN1);
        OntModel m1 = getOntologyModel(ONT_1_OWL);
        OntModel m = getOntologyModel(ONT_2_OWL);
        //printClasses(m);

        int id = 0;
        avoided = avoidedClass();
        /*
        N..1 Case
         */
        ResIterator it = model1.listSubjects();
        while (it.hasNext()) {
            Resource r = it.nextResource();
            /*
            if (r.toString().contains(ONT_1_LINK)) {
                // System.out.println(r);
                continue;
            }
            */


            /**
             * Cette partie du code permet de ne traiter que les individus des classes non évitées, obtenues par la méthode avoidedClass()
             * La première ligne permet de récupérer les triplets tels que leurs prédicats soient égaux à ce que est précisé dans (createProperty())
             * (i.e le lien vers le type, ou classe, dans ce cas)
             */
            Statement rType = r.getProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
            if (rType != null) {
                if (avoided.contains(rType.getObject().toString()))
                    continue;
            }

            StmtIterator properties = r.listProperties();
            while (properties.hasNext()) {

                Statement p = properties.nextStatement();

                Resource sub = p.getSubject();
                Property prd = p.getPredicate();
                RDFNode obj = p.getObject();


                if (entitiesN1.contains(p.getPredicate().toString())) {
                    Pair<HashSet<Statement>, HashSet<Statement>> pp = getMappedURI(set, prd.toString());

                    String ent1URI = null;
                    String ent2URI = pp.getRight().iterator().next().getObject().toString();

                    for (Statement st : pp.getLeft()) {
                        if (st.getPredicate().toString().contains("#entity2")) {
                            ent1URI = st.getObject().toString();
                        }
                    }
                    for (Statement st : pp.getLeft()) {
                        Resource indv = model1.getResource(p.getObject().toString());

                        Property prd1 = ResourceFactory.createProperty(ent2URI);
                        obj = ResourceFactory.createStringLiteral(indv.getProperty(ResourceFactory.createProperty(ent1URI)).getObject().toString());//
                        Statement s = ResourceFactory.createStatement(sub, prd1, obj);
                        //System.out.println(s);
                        model1 = model1.remove(p);
                        model1 = model1.add(s);
                        properties = r.listProperties();
                    }
                }
            }
        }
        /*
          1..N Case
         */
        it = model1.listSubjects();
        while (it.hasNext()) {
            Resource r = it.nextResource();
            Statement rType = r.getProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
            if (rType != null) {
                if (avoided.contains(rType.getObject().toString()))
                    continue;
            }

            StmtIterator properties = r.listProperties();
            while (properties.hasNext()) {
                Statement p = properties.nextStatement();
                Resource sub = p.getSubject();
                Property prd = p.getPredicate();
                RDFNode obj = p.getObject();

                if (entities1N.contains(p.getPredicate().toString())) {
                    Pair<HashSet<Statement>, HashSet<Statement>> pp = getMappedURI(set, prd.toString());

                    String ent1URI = null;
                    String ent2URI = null;
                    for (Statement st : pp.getRight()) {
                        if (st.getPredicate().toString().contains("#entity1")) {
                            ent1URI = st.getObject().toString();
                        }

                        if (st.getPredicate().toString().contains("#entity2")) {
                            ent2URI = st.getObject().toString();
                        }
                    }

                    boolean create = true;
                    DatatypeProperty prp = null;
                    Individual ind = null;
                    prd = ResourceFactory.createProperty(ent1URI);
                    if (m.getOntClass(ONT_2_LINK + "#" + StringUtils.capitalize(p.getPredicate().toString().split("#")[1])) == null) {
                        m.createClass(ONT_2_LINK + "#" + StringUtils.capitalize(p.getPredicate().toString().split("#")[1]));
                    }

                    for (Individual k : m.listIndividuals().toList()) {
                        if (create == false)
                            break;

                        if (k.getOntClass().equals(m.getOntClass(ONT_2_LINK + "#" + StringUtils.capitalize(p.getPredicate().toString().split("#")[1])))) {
                            for (Statement prop : k.listProperties().toList()) {
                                if (prop.getPredicate().toString().equals(ent2URI) && prop.getObject().toString().equals(p.getObject().toString())) {
                                    //create = false;
                                    m.getDatatypeProperty(prop.getPredicate().toString());
                                    obj = ResourceFactory.createProperty(k.getURI());
                                    create = false;
                                    break;
                                }
                            }
                        }
                    }

                    if (create == true) {
                        ind = m.createIndividual(ONT_2_LINK + "#" + StringUtils.capitalize(p.getPredicate().toString().split("#")[1]) + id++, m.getOntClass(ONT_2_LINK + "#" + StringUtils.capitalize(p.getPredicate().toString().split("#")[1])));
                        prp = m.createDatatypeProperty(ent2URI);
                        ind.addProperty(prp, m.createLiteral(p.getObject().toString()));
                        obj = ResourceFactory.createProperty(ind.getURI());
                    }

                    Statement s = ResourceFactory.createStatement(sub, prd, obj);
                    //System.out.println(s);
                    model1 = model1.remove(p);
                    model1 = model1.add(s);
                    properties = r.listProperties();
                }
            }
        }

        /*
         * 1..1 Case
         */
        it = model1.listSubjects();
        while (it.hasNext()) {
            Resource r = it.nextResource();
            Statement rType = r.getProperty(ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
            if (rType != null) {
                if (avoided.contains(rType.getObject().toString()))
                    continue;
            }

            StmtIterator properties = r.listProperties();
            while (properties.hasNext()) {
                Statement p = properties.nextStatement();
                Resource sub = p.getSubject();
                Property prd = p.getPredicate();
                RDFNode obj = p.getObject();

                if (entities11.contains(prd.toString())) {
                    prd = ResourceFactory.createProperty(getMappedURI(set, prd.toString()).getRight().iterator().next().getObject().toString());
                    Statement s = ResourceFactory.createStatement(sub, prd, obj);
                    model1 = model1.remove(p);
                    model1 = model1.add(s);
                    properties = r.listProperties();
                }
            }

            properties = r.listProperties();
            while (properties.hasNext()) {
                Statement st = properties.nextStatement();
                /*
                if (st.getSubject().toString().contains(ONT_1_LINK) && st.getSubject().toString().split("#")[1].charAt(0) <= 'z' && st.getSubject().toString().split("#")[1].charAt(0) >= 'a') {
                    System.out.println(st);
                    continue;
                }
                */
                m.add(st);
            }
        }

        outputModel(m);
    }

    /**
     * Permet de charger l'ontologie depuis un fichier (version OWL1)
     *
     * @param ontoFile
     * @return
     */
    private static OntModel getOntologyModel(String ontoFile) {
        OntModel ontoModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        try {
            InputStream in = FileManager.get().open(ontoFile);
            try {
                ontoModel.read(in, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //System.out.println("Ontology " + ontoFile + " loaded.");
        } catch (JenaException je) {
            System.err.println("ERROR" + je.getMessage());
            je.printStackTrace();
            System.exit(0);
        }
        return ontoModel;
    }
}

package reecriture;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.JenaException;
import org.apache.jena.util.FileManager;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;

public class Mappeur {
    // Mapping from Restaurant1 -> Restaurant2
    private final static String NOM_FICHIER_ONTOLOGIE_SRC = "data/ontology_restaurant1_rdf.owl";
    private final static String NOM_FICHIER_ONTOLOGIE_DEST = "data/ontology_restaurant2_rdf.owl";
    private static String URI_ONTOLOGIE_SRC;
    private static String URI_ONTOLOGIE_RESULTAT;

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

    /**
     * Permet de retourner le lien de la première ontologie
     *
     * @return
     */
    private static String getOntlogyLink1(String NOM_FICHIER_ONTOLOGIE, String URI_ONTOLOGIE) {
        OntModel m = getOntologyModel(NOM_FICHIER_ONTOLOGIE);
        for (OntClass i : m.listClasses().toList()) {
            if (URI_ONTOLOGIE == null) {
                return i.toString().split("#")[0];
            }
        }

        return null;
    }

    private static HashSet<Pair<HashSet<Statement>, HashSet<Statement>>> generateSets(Model modeleMappage) {
        HashSet<Pair<HashSet<Statement>, HashSet<Statement>>> ret = new HashSet<>();

        ResIterator subject = modeleMappage.listSubjects();
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

        subject = modeleMappage.listSubjects();
        while (subject.hasNext()) {
            Resource r = subject.next();
            StmtIterator properties = r.listProperties();

            while (properties.hasNext()) {
                Statement p = properties.nextStatement();

                if (p.getPredicate().toString().contains("mapTo")) {
                    HashSet<Statement> hs1 = hm.get(p.getSubject().toString());
                    HashSet<Statement> hs2 = hm.get(p.getObject().toString());
                    if (!hs1.iterator().next().getPredicate().toString().contains(URI_ONTOLOGIE_SRC)) {
                        ret.add(new ImmutablePair<>(hs1, hs2));
                    } else {
                        ret.add(new ImmutablePair<>(hs2, hs1));
                    }

                }
            }
        }

        return ret;
    }

    private HashSet<String> genererEntites(HashSet<Pair<HashSet<Statement>, HashSet<Statement>>> s, NatureMappage nature) {
        HashSet<String> ret = new HashSet<>();
        if (NatureMappage.UN_A_UN.equals(nature)) {
            for (Pair<HashSet<Statement>, HashSet<Statement>> p : s) {
                HashSet<String> hs1 = new HashSet<>();
                HashSet<String> hs2 = new HashSet<>();

                p.getLeft().forEach(st -> hs1.add((st.getPredicate()).toString()));
                p.getRight().forEach(st -> hs2.add((st.getPredicate()).toString()));

                if (hs1.size() == 1 && hs2.size() == 1) {
                    ret.add(p.getLeft().iterator().next().getObject().toString());
                }
            }

        } else if (NatureMappage.UN_A_PLUSIEURS.equals(nature)) {
            for (Pair<HashSet<Statement>, HashSet<Statement>> p : s) {
                HashSet<String> hs1 = new HashSet<>();
                HashSet<String> hs2 = new HashSet<>();

                p.getLeft().forEach(st -> hs1.add((st.getPredicate()).toString()));
                p.getRight().forEach(st -> hs2.add((st.getPredicate()).toString()));

                if (hs1.size() == 1 && hs2.size() > 1) {
                    ret.add(p.getLeft().iterator().next().getObject().toString());
                }
            }

        } else {
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

        }
        return ret;
    }


}

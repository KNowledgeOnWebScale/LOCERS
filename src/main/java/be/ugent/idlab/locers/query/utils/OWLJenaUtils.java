/**
 * 
 */
package be.ugent.idlab.locers.query.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;


public class OWLJenaUtils {
	final static Logger logger = LoggerFactory.getLogger(OWLJenaUtils.class);

	public static OWLOntology getOWLOntology(final Model model) {
		OWLOntology ontology;
		try (PipedInputStream is = new PipedInputStream(); PipedOutputStream os = new PipedOutputStream(is)) {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			new Thread(new Runnable() {
				@Override
				public void run() {
					model.write(os, "TURTLE", null);
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
			ontology = man.loadOntologyFromOntologyDocument(is);
			return ontology;
		} catch (Exception e) {
			throw new RuntimeException("Could not convert JENA API model to OWL API ontology.", e);
		}
	}

	public static OntModel getOntologyModel(OWLOntologyManager manager, OWLOntology ontology) {
		OntModel noReasoningModel = null;

		noReasoningModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		noReasoningModel.getDocumentManager().setProcessImports(false);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			manager.saveOntology(ontology,  out);
		} catch (OWLOntologyStorageException e) {
			logger.error("Unable to write ontology to stream");
		}

		try {
			noReasoningModel.read(new ByteArrayInputStream(out.toByteArray()), "RDF/XML");
		} catch (Exception e) {
			logger.error("Problems reading stream. Might be ignored");
		}

		return noReasoningModel;
	}

	public static OntModel getOntologyModel(OWLOntologyManager manager, Set<OWLAxiom> axioms) {
		OntModel model = null;
		try {
			OWLOntology temp = manager.createOntology();
			manager.addAxioms(temp, axioms);
			model = getOntologyModel(manager, temp);
			manager.removeOntology(temp);
		} catch (OWLOntologyCreationException e) {
			logger.error("Unable to create empty ontology", e);
		}
		return model;
	}

	public static Set<OWLAxiom> checkForIncorrectAnnotations(Set<OWLAxiom> axioms, OWLOntology ontology) {
		Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		Set<OWLOntology> allOnts = ontology.getImports();
		allOnts.add(ontology);
		for (OWLAxiom ax : axioms) {
			boolean found = false;
			for (OWLOntology ont : allOnts) {
				if (ax instanceof OWLAnnotationAssertionAxiom) {
					OWLAnnotationAssertionAxiom anno = (OWLAnnotationAssertionAxiom) ax;
					OWLIndividual subject = new OWLNamedIndividualImpl(IRI.create(anno.getSubject().toString()));
					if (ont.containsObjectPropertyInSignature(anno.getProperty().getIRI())) {
						OWLIndividual object = new OWLNamedIndividualImpl(IRI.create(anno.getValue().toString()));
						OWLObjectProperty objProp = new OWLObjectPropertyImpl(anno.getProperty().getIRI());

						newAxioms.add(manager.getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(objProp, subject,
								object));
						found = true;
						break;
					} else if (ont.containsDataPropertyInSignature(anno.getProperty().getIRI())) {
						OWLDataProperty dataProp = new OWLDataPropertyImpl(anno.getProperty().getIRI());
						newAxioms.add(manager.getOWLDataFactory().getOWLDataPropertyAssertionAxiom(dataProp, subject,
								(OWLLiteral) anno.getValue()));
						found = true;
						break;
					}
				}
			}
			// if(ax instanceof OWLDeclarationAxiom){
			// System.out.println(ax);
			// found = true;
			// }
			if (!found) {
				newAxioms.add(ax);
			}
		}

		return newAxioms;
	}
	public static Model queryConstruct(Query query, Model model) {
		try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
			Model result = qexec.execConstruct();
			return result;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static List<Map<String, String>> query(Query query, Model merge) {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		try (QueryExecution qexec = QueryExecutionFactory.create(query, merge)) {
			if (query.isSelectType()) {
				ResultSet result = qexec.execSelect();

				while (result != null && result.hasNext()) {
					Map<String, String> tempMap = new HashMap<String, String>();

					QuerySolution solution = result.next();
					Iterator<String> it = solution.varNames();

					// Iterate over all results
					while (it.hasNext()) {
						String varName = it.next();
						String varValue = solution.get(varName).toString();
						tempMap.put(varName, varValue);

					}

					// Only add if we have some objects in temp map
					if (tempMap.size() > 0) {
						results.add(tempMap);
					}
				}
			} else {
				Model result = qexec.execConstruct();
				System.out.println(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}
	public static OWLOntology materialize(OWLOntology ontology, Reasoner reasoner) {
		long time1 = System.currentTimeMillis();
		reasoner.flush();
		List<InferredAxiomGenerator<? extends OWLAxiom>> generators = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
		// generators.add(new InferredSubClassAxiomGenerator());
		// generators.add(new InferredInverseObjectPropertiesAxiomGenerator());
		// generators.add(new InferredPropertyAssertionGenerator());
		generators.add(new InferredClassAssertionAxiomGenerator());
		// generators.add(new InferredSubObjectPropertyAxiomGenerator());
		InferredOntologyGenerator infGen = new InferredOntologyGenerator(reasoner, generators);
		infGen.fillOntology(ontology.getOWLOntologyManager().getOWLDataFactory(), ontology);

		System.out.println("Mat time: " + (System.currentTimeMillis() - time1));
		return ontology;
	}
}

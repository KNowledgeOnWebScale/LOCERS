/**
 * 
 */
package be.ugent.idlab.locers.test;

import be.ugent.idlab.locers.cache.HashCacheStructure;
import be.ugent.idlab.locers.cache.LOCERSStructureCache;
import be.ugent.idlab.locers.examples.MemUtils;
import be.ugent.idlab.locers.query.CacheQuery;
import be.ugent.idlab.locers.query.CacheQueryGenerator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class ScalabilityTest {

	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLDataFactory dataFactory = manager.getOWLDataFactory();

	public static void main(String[] args) throws Exception {
		final int numTest = 1000;
		final int numRel = 100;
		File inputOntologyFile = new File("resources/example.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputOntologyFile);

		LOCERSStructureCache cache = new LOCERSStructureCache();
		cache.init(ontology);
		cache.setCacheStructure(new HashCacheStructure());

		OWLObjectSomeValuesFrom objRestiriction = dataFactory.getOWLObjectSomeValuesFrom(
				dataFactory.getOWLObjectProperty("http://knowman.idlab.ugent.be/example#hasSupPart"),
				dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#BackSupport"));
		OWLObjectIntersectionOf objRestiriction2 = dataFactory.getOWLObjectIntersectionOf(
				dataFactory.getOWLObjectSomeValuesFrom(
						dataFactory.getOWLObjectProperty("http://knowman.idlab.ugent.be/example#hasSupPart"),
						dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Leg")),
				dataFactory.getOWLObjectSomeValuesFrom(
						dataFactory.getOWLObjectProperty("http://knowman.idlab.ugent.be/example#hasSupPart"),
						dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#BackSupport")));
		OWLClass test = dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Test");
		OWLClass test2 = dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Test2");

		cache.addCQ(test, objRestiriction);
		cache.addCQ(test2, objRestiriction2);

		adaptTboxForTestExtending(numTest, numRel, ontology);
		List<Long> results = new ArrayList<Long>();
		for (int i = 1; i < numTest; i++) {
			pupulateCache(cache, ontology, generateEventExtending(i, numRel));

		}
		System.out.println("done populating");
		for (int i = 1; i < 35; i++) {
			Set<OWLAxiom> event = generateEventExtending(i * 10 , numRel);
			long time1 = System.currentTimeMillis();
			cache.check(event);
			long end = System.currentTimeMillis();
			long result = ( end- time1);
			System.out.println(result);
			results.add(result);
			System.out.println("Memory: "+ MemUtils.getReallyUsedMemory());
			System.out.println("Cache size: " +cache.getSize());


		}
		System.out.println("Avg: " + results.stream().mapToDouble(a -> a).average().getAsDouble());
		System.out.println(cache.getSize());
		System.out.println(results);
	}

	public static void adaptTboxForTest(int numTests, OWLOntology ontology) {
		String iri = "http://knowman.idlab.ugent.be/example#";
		for (int j = 0; j < numTests; j++) {
			manager.addAxiom(ontology,
					dataFactory.getOWLDeclarationAxiom(dataFactory.getOWLObjectProperty(iri + "hasSupPart" + j)));
		}
	}

	public static void adaptTboxForTestExtending(int numTests, int numRelations, OWLOntology ontology) {
		String iri = "http://knowman.idlab.ugent.be/example#";
		for (int j = 0; j < numRelations; j++) {
			manager.addAxiom(ontology, dataFactory
					.getOWLDeclarationAxiom(dataFactory.getOWLObjectProperty(iri + "hasSupPart" + numTests + "_" + j)));
		}
	}

	public static Set<OWLAxiom> generateEventExtending(int i, int numRelations) {
		String iri = "http://knowman.idlab.ugent.be/example#";
		Set<OWLAxiom> event = new HashSet<OWLAxiom>();
		event.add(dataFactory.getOWLObjectPropertyAssertionAxiom(dataFactory.getOWLObjectProperty(iri + "hasSupPart"),
				dataFactory.getOWLNamedIndividual(iri + "chair2"),
				dataFactory.getOWLNamedIndividual(iri + "backSupport")));
		for (int j = 0; j < numRelations; j++) {
			event.add(dataFactory.getOWLObjectPropertyAssertionAxiom(
					dataFactory.getOWLObjectProperty(iri + "hasSupPart" + i + "_" + j),
					dataFactory.getOWLNamedIndividual(iri + "chair2"),
					dataFactory.getOWLNamedIndividual(iri + "backSupport" + i + "_" + j)));

		}
		return event;
	}

	public static Set<OWLAxiom> generateEvent(int i) {
		String iri = "http://knowman.idlab.ugent.be/example#";
		Set<OWLAxiom> event = new HashSet<OWLAxiom>();
		event.add(dataFactory.getOWLObjectPropertyAssertionAxiom(dataFactory.getOWLObjectProperty(iri + "hasSupPart"),
				dataFactory.getOWLNamedIndividual(iri + "chair2"),
				dataFactory.getOWLNamedIndividual(iri + "backSupport")));
		int j = i;
		event.add(
				dataFactory.getOWLObjectPropertyAssertionAxiom(dataFactory.getOWLObjectProperty(iri + "hasSupPart" + j),
						dataFactory.getOWLNamedIndividual(iri + "chair2"),
						dataFactory.getOWLNamedIndividual(iri + "backSupport" + j)));

		return event;
	}

	public static Set<OWLAxiom> generateEvent2() {
		Set<OWLAxiom> event = generateEvent(2);
		String iri = "http://knowman.idlab.ugent.be/example#";
		event.add(dataFactory.getOWLObjectPropertyAssertionAxiom(dataFactory.getOWLObjectProperty(iri + "hasSupPart"),
				dataFactory.getOWLNamedIndividual(iri + "chair2"), dataFactory.getOWLNamedIndividual(iri + "leg2")));
		return event;
	}

	public static void pupulateCache(LOCERSStructureCache cache, OWLOntology ontology, Set<OWLAxiom> event) {
		OWLOntology tempOnt;
		try {
			tempOnt = manager.createOntology();
			manager.addAxioms(tempOnt, event);
			Set<OWLAxiom> extended = tempOnt.individualsInSignature().map(ind -> ontology.classAssertionAxioms(ind))
					.flatMap(Function.identity()).collect(Collectors.toSet());

			extended.addAll(event);

			CacheQuery q = CacheQueryGenerator.generate(extended);
			cache.getCacheStructure().add(q, Collections.singleton(dataFactory.getOWLClass("owl:Thing")));
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

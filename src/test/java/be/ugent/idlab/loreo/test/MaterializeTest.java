/**
 * 
 */
package be.ugent.idlab.loreo.test;

import be.ugent.idlab.loreo.cache.*;
import be.ugent.idlab.loreo.query.CacheQuery;
import be.ugent.idlab.loreo.query.CacheQueryGenerator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author pbonte
 *
 */
public class MaterializeTest {

	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLDataFactory dataFactory = manager.getOWLDataFactory();

	public static void main(String[] args) throws Exception {
		final int numTest = 1000;
		final int numRel = 400;
		File inputOntologyFile = new File("resources/lubm.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputOntologyFile);

		LOREOMaterializeCache cache = new LOREOMaterializeCache();
		cache.init(ontology);
		cache.setCacheStructure(new MaterializeCacheStructure());
//
//		OWLObjectSomeValuesFrom objRestiriction = dataFactory.getOWLObjectSomeValuesFrom(
//				dataFactory.getOWLObjectProperty("http://knowman.idlab.ugent.be/example#hasSupPart"),
//				dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#BackSupport"));
//		OWLObjectIntersectionOf objRestiriction2 = dataFactory.getOWLObjectIntersectionOf(
//				dataFactory.getOWLObjectSomeValuesFrom(
//						dataFactory.getOWLObjectProperty("http://knowman.idlab.ugent.be/example#hasSupPart"),
//						dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Leg")),
//				dataFactory.getOWLObjectSomeValuesFrom(
//						dataFactory.getOWLObjectProperty("http://knowman.idlab.ugent.be/example#hasSupPart"),
//						dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#BackSupport")));

		for(int i =0 ;i <1;i++) {
			Set<OWLAxiom> events1 = generateStudentMultiple(1, 0+i, dataFactory);
			Set<OWLAxiom> events2 = generateStudentMultiple(1, 5+i, dataFactory);
			Set<OWLAxiom> events3 = generateStudentMultiple(1, 2000+i, dataFactory);
			Set<OWLAxiom> events4 = generateStudentMultiple(1, 3000+i, dataFactory);
			long time0 = System.currentTimeMillis();
			Set<OWLAxiom> result = cache.check(events1);
			long firstCheckTime = System.currentTimeMillis() - time0;
			System.out.println(result.size());
			long time1 = System.currentTimeMillis();

			Set<OWLAxiom> result2 = cache.check(events2);
			System.out.println(result2.size());
			System.out.println("Time Cache1: " + firstCheckTime);
			System.out.println("Time Cache2: " + (System.currentTimeMillis() - time1));
			time1 = System.currentTimeMillis();
			cache.check(events3);
			System.out.println("Time Cache3: " + (System.currentTimeMillis() - time1));
			time1 = System.currentTimeMillis();
			Set<OWLAxiom> result4= cache.check(events4);
			//System.out.println(result4);
			System.out.println("Time Cache4: " + (System.currentTimeMillis() - time1));
			assertEquals(result.size(),result2.size());
		}


	}

	public static Set<OWLAxiom> generateStudent(int id,OWLDataFactory factory){
		String iriGradStud = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#UndergraduateStudent";
		String iriMemberOf = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#memberOf";
		String iriDepartment = "http://www.Department2.University1.edu";
		
		OWLClass gradStudClass = factory.getOWLClass(iriGradStud);
		OWLObjectProperty memberOfProp = factory.getOWLObjectProperty(iriMemberOf);
		OWLNamedIndividual departmentInd = factory.getOWLNamedIndividual(iriDepartment);
		
		Set<OWLAxiom> event = new HashSet<OWLAxiom>();
		OWLNamedIndividual newStud = factory.getOWLNamedIndividual(iriGradStud+"_"+id);
		event.add(factory.getOWLClassAssertionAxiom(gradStudClass, newStud));
		event.add(factory.getOWLObjectPropertyAssertionAxiom(memberOfProp, newStud, departmentInd));
		
		return event;
	}
	public static Set<OWLAxiom> generateStudentMultiple(int number,int start,OWLDataFactory factory){
		String iriGradStud = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#UndergraduateStudent";
		String iriMemberOf = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#memberOf";
		String iriDepartment = "http://www.Department2.University1.edu";
		String hasValue = "http://example.org/lubm.owl#hasValue";
		Set<OWLAxiom> event = new HashSet<OWLAxiom>();
		OWLClass gradStudClass = factory.getOWLClass(iriGradStud);
		OWLObjectProperty memberOfProp = factory.getOWLObjectProperty(iriMemberOf);
		OWLNamedIndividual departmentInd = factory.getOWLNamedIndividual(iriDepartment);
		OWLDataProperty hasValueProp = factory.getOWLDataProperty(hasValue);
		
		for(int i = 0 ; i <number;i++){
			
		OWLNamedIndividual newStud = factory.getOWLNamedIndividual(iriGradStud+"Test_"+i+start);
		event.add(factory.getOWLClassAssertionAxiom(gradStudClass, newStud));
		event.add(factory.getOWLObjectPropertyAssertionAxiom(memberOfProp, newStud, departmentInd));
		event.add(factory.getOWLDataPropertyAssertionAxiom(hasValueProp, newStud, 1+start));

		}
		
		return event;
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

	public static void pupulateCache(LOREOStructureCache cache, OWLOntology ontology, Set<OWLAxiom> event) {
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

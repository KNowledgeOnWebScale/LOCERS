/**
 * 
 */
package be.ugent.idlab.locers.test;

import be.ugent.idlab.locers.cache.HashCacheStructure;
import be.ugent.idlab.locers.cache.LOCERSStructureCache;
import be.ugent.idlab.locers.query.AxiomIndexer;
import be.ugent.idlab.locers.query.CacheQuery;
import be.ugent.idlab.locers.query.CacheQueryGenerator2;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;


public class StaticDataTest {

	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLDataFactory dataFactory = manager.getOWLDataFactory();

	public static void main(String[] args) throws Exception {
		final int numTest = 1000;
		final int numRel = 400;
		File inputOntologyFile = new File("resources/lubm.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputOntologyFile);

		LOCERSStructureCache cache = new LOCERSStructureCache();
		cache.init(ontology);
		cache.setCacheStructure(new HashCacheStructure());

		OWLClass test = dataFactory.getOWLClass("http://example.org/lubm.owl#Query8");
	    for(OWLEquivalentClassesAxiom eq: ontology.getEquivalentClassesAxioms(test)){
	    	for(OWLClassExpression cE: eq.getClassExpressions()){
	    	if(!cE.toString().equals(test.toString())){
	    		cache.addCQ(test, cE);
	    	}
	    	}
	    	//cache.addCQ(test, );
	    }
		String iriDepartment = "http://www.Department2.University1.edu";
		Set<OWLNamedIndividual> statics = new HashSet<OWLNamedIndividual>();
		statics.add(manager.getOWLDataFactory().getOWLNamedIndividual(iriDepartment));
	    Set<OWLAxiom> events1 = generateStudentMultiple(10, 0, dataFactory,1);
	    Set<OWLAxiom> events2 = generateStudentMultiple(10, 200, dataFactory,2);
		OWLOntology tempOnt = manager.createOntology();

		manager.addAxioms(tempOnt, events1);
		// first check the cache
		Set<OWLAxiom> extended = tempOnt.individualsInSignature().map(ind -> ontology.classAssertionAxioms(ind))
				.flatMap(Function.identity()).collect(Collectors.toSet());
		extended.addAll(events1);
		CacheQuery q = CacheQueryGenerator2.generate(extended,statics);
		AxiomIndexer indexer = new AxiomIndexer();
		try {
			indexer.addAll(extended);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean found = indexer.checkQuery(q);
		System.out.println(found);

		tempOnt = manager.createOntology();

		manager.addAxioms(tempOnt, events2);
		// first check the cache
		extended = tempOnt.individualsInSignature().map(ind -> ontology.classAssertionAxioms(ind))
				.flatMap(Function.identity()).collect(Collectors.toSet());
		extended.addAll(events2);
		AxiomIndexer indexer2 = new AxiomIndexer();
		try {
			indexer2.addAll(extended);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		found = indexer2.checkQuery(q);
		System.out.println(found);


//		cache.addCQ(test, objRestiriction);
//		cache.addCQ(test2, objRestiriction2);

	}

	public static Set<OWLAxiom> generateStudent(int id,OWLDataFactory factory,int depId){
		String iriGradStud = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#UndergraduateStudent";
		String iriMemberOf = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#memberOf";
		String iriDepartment = "http://www.Department2.University"+depId+".edu";
		
		OWLClass gradStudClass = factory.getOWLClass(iriGradStud);
		OWLObjectProperty memberOfProp = factory.getOWLObjectProperty(iriMemberOf);
		OWLNamedIndividual departmentInd = factory.getOWLNamedIndividual(iriDepartment);
		
		Set<OWLAxiom> event = new HashSet<OWLAxiom>();
		OWLNamedIndividual newStud = factory.getOWLNamedIndividual(iriGradStud+"_"+id);
		event.add(factory.getOWLClassAssertionAxiom(gradStudClass, newStud));
		event.add(factory.getOWLObjectPropertyAssertionAxiom(memberOfProp, newStud, departmentInd));
		
		return event;
	}
	public static Set<OWLAxiom> generateStudentMultiple(int number,int start,OWLDataFactory factory,int depId){
		String iriGradStud = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#UndergraduateStudent";
		String iriMemberOf = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#memberOf";
		String iriDepartment = "http://www.Department2.University"+depId+".edu";
		Set<OWLAxiom> event = new HashSet<OWLAxiom>();
		OWLClass gradStudClass = factory.getOWLClass(iriGradStud);
		OWLObjectProperty memberOfProp = factory.getOWLObjectProperty(iriMemberOf);
		OWLNamedIndividual departmentInd = factory.getOWLNamedIndividual(iriDepartment);
		
		for(int i = 0 ; i <number;i++){
			
		OWLNamedIndividual newStud = factory.getOWLNamedIndividual(iriGradStud+"Test_"+i+start);
		event.add(factory.getOWLClassAssertionAxiom(gradStudClass, newStud));
		event.add(factory.getOWLObjectPropertyAssertionAxiom(memberOfProp, newStud, departmentInd));
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





}

package be.ugent.idlab.loreo.test;

import be.ugent.idlab.loreo.cache.HashCacheStructure;
import be.ugent.idlab.loreo.cache.LOREOCache;
import be.ugent.idlab.loreo.cache.LOREONaiveCache;
import be.ugent.idlab.loreo.cache.LOREOStructureCache;
import be.ugent.idlab.loreo.query.AxiomIndexer;
import be.ugent.idlab.loreo.query.CacheQuery;
import be.ugent.idlab.loreo.query.CacheQueryGenerator;
import org.junit.Test;
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
public class QueryTest {


	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLDataFactory dataFactory = manager.getOWLDataFactory();

	@Test
	public void queryTest() throws Exception{
		File inputOntologyFile = new File("resources/lubm.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputOntologyFile);

		LOREOStructureCache cache = new LOREOStructureCache();
		cache.init(ontology);
		cache.setCacheStructure(new HashCacheStructure());



		Set<OWLAxiom> events1 = generateStudentMultiple(1595, 0, dataFactory);

		OWLOntology tempOnt = manager.createOntology();

		manager.addAxioms(tempOnt, events1);
		// first check the cache
		Set<OWLAxiom> extended = tempOnt.individualsInSignature().map(ind -> ontology.classAssertionAxioms(ind))
				.flatMap(Function.identity()).collect(Collectors.toSet());
		extended.addAll(events1);
		CacheQuery q = CacheQueryGenerator.generate(extended);
		//System.out.println(q);
		AxiomIndexer indexer = new AxiomIndexer();
		try {
			indexer.addAll(extended);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean found = indexer.checkQuery(q);
		System.out.println(indexer.getNumberOfIndividuals());
		System.out.println(q.getNumUsedIndividual());
		//System.out.println(q);

		assertEquals(indexer.getNumberOfIndividuals(),q.getNumUsedIndividual());
	
	}
	public static Set<OWLAxiom> generateStudentMultiple(int number,int start,OWLDataFactory factory){
		String iriGradStud = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#UndergraduateStudent";
		String iriMemberOf = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#memberOf";
		String iriDepartment = "http://www.Department2.University1.edu";
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
}

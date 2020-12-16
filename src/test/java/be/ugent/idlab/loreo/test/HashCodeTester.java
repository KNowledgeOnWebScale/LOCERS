package be.ugent.idlab.loreo.test;

import be.ugent.idlab.loreo.cache.HashCacheStructure;
import be.ugent.idlab.loreo.query.AxiomIndexer;
import be.ugent.idlab.loreo.query.CacheQuery;
import be.ugent.idlab.loreo.query.CacheQueryGenerator;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author pbonte
 *
 */
public class HashCodeTester {
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLDataFactory dataFactory = manager.getOWLDataFactory();

	@Test
	public void hashTest() throws Exception{
		CacheQuery cq = CacheQueryGenerator.generate(generateEventExtending2(1, 10));
		CacheQuery cq2 = CacheQueryGenerator.generate(generateEventExtending2(2, 10));
		assertEquals(true, cq.generateHashCode()==cq2.generateHashCode());
		CacheQuery cq3 = CacheQueryGenerator.generate(generateEventExtending2(1, 10));
		CacheQuery cq4 = CacheQueryGenerator.generate(generateEventExtending2(2, 110));
		assertEquals(false, cq3.generateHashCode()==cq4.generateHashCode());
		CacheQuery cq5 = CacheQueryGenerator.generate(generateEventExtending2(1, 10));
		CacheQuery cq6 = CacheQueryGenerator.generate(generateEventExtending2(1, 10));
		assertEquals(true, cq5.generateHashCode()==cq6.generateHashCode());
		CacheQuery speedTest = CacheQueryGenerator.generate(generateEventExtending2(1, 50000));
		long time1=System.currentTimeMillis();
		int hash = speedTest.generateHashCode();
		System.out.println(hash+" "+(System.currentTimeMillis()-time1));
		Set<OWLAxiom> event = generateEventExtending2(1, 500000);
		
		time1=System.currentTimeMillis();
		int code2=HashCacheStructure.generateHashFor(event);
		System.out.println(code2+" genering hash "+(System.currentTimeMillis()-time1));
	
		time1=System.currentTimeMillis();
		CacheQuery q = CacheQueryGenerator.generate(event);
		int code1=q.generateHashCode();
		System.out.println(code1 +" genering query "+(System.currentTimeMillis()-time1));
		time1=System.currentTimeMillis();
		
		AxiomIndexer indexer = new AxiomIndexer();
		try {
			indexer.addAll(event);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(code1 +" indexing "+(System.currentTimeMillis()-time1));

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
	public static Set<OWLAxiom> generateEventExtending2(int i, int numRelations) {
		String iri = "http://knowman.idlab.ugent.be/example#";
		Set<OWLAxiom> event = new HashSet<OWLAxiom>();
		event.add(dataFactory.getOWLObjectPropertyAssertionAxiom(dataFactory.getOWLObjectProperty(iri + "hasSupPart"),
				dataFactory.getOWLNamedIndividual(iri + "chair2"),
				dataFactory.getOWLNamedIndividual(iri + "backSupport")));
		for (int j = 0; j < numRelations; j++) {
			event.add(dataFactory.getOWLObjectPropertyAssertionAxiom(
					dataFactory.getOWLObjectProperty(iri + "hasSupPart" +  "_" + j),
					dataFactory.getOWLNamedIndividual(iri + "chair2"+i+1),
					dataFactory.getOWLNamedIndividual(iri + "backSupport" + i + "_" + j)));

		}
		return event;
	}
}

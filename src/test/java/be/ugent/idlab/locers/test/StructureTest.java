/**
 * 
 */
package be.ugent.idlab.locers.test;

import be.ugent.idlab.locers.cache.LOCERSCache;
import be.ugent.idlab.locers.cache.LOCERSStructureCache;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;


public class StructureTest {
	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	OWLDataFactory dataFactory = manager.getOWLDataFactory();
	@Test
	public void explainTest() throws Exception{
		File inputOntologyFile = new File("resources/example.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputOntologyFile);
		
		
		LOCERSCache cache = new LOCERSStructureCache();
		cache.init(ontology);
		OWLClass test = dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Test");
		OWLClass test2 = dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Test2");

		OWLObjectSomeValuesFrom objRestiriction = dataFactory.getOWLObjectSomeValuesFrom(dataFactory.getOWLObjectProperty("http://knowman.idlab.ugent.be/example#hasSupPart"), dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#BackSupport"));
		OWLObjectIntersectionOf objRestiriction2 = dataFactory.getOWLObjectIntersectionOf(dataFactory.getOWLObjectSomeValuesFrom(dataFactory.getOWLObjectProperty("http://knowman.idlab.ugent.be/example#hasSupPart"), dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Leg")),
				dataFactory.getOWLObjectSomeValuesFrom(dataFactory.getOWLObjectProperty("http://knowman.idlab.ugent.be/example#hasSupPart"), dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#BackSupport")));

		cache.addCQ(test, objRestiriction);
		cache.addCQ(test2, objRestiriction2);

		long time1= System.currentTimeMillis();
		Set<OWLClass> targets = cache.check(generateEvent(2));
		System.out.println((System.currentTimeMillis()-time1)+" "+targets);
		time1=System.currentTimeMillis();
		System.out.println(cache.check(generateEvent(2))+ "\n "+(System.currentTimeMillis()-time1));
		time1=System.currentTimeMillis();
		System.out.println(cache.check(generateEvent2())+ "\n "+(System.currentTimeMillis()-time1));
		time1=System.currentTimeMillis();
		System.out.println(cache.check(generateEventNoHit())+ "\n "+(System.currentTimeMillis()-time1));
		time1=System.currentTimeMillis();
		System.out.println(cache.check(generateEventNoHit())+ "\n "+(System.currentTimeMillis()-time1));
	}
	
	@Test
	public void sameStructureTest() throws Exception{
		
		File inputOntologyFile = new File("resources/example.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputOntologyFile);
		
		
		LOCERSCache cache = new LOCERSStructureCache();
		cache.init(ontology);
		
		OWLClass test = dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Test");
		OWLObjectSomeValuesFrom objRestiriction = dataFactory.getOWLObjectSomeValuesFrom(dataFactory.getOWLObjectProperty("http://knowman.idlab.ugent.be/example#hasSupPart"), dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#BackSupport"));
		cache.addCQ(test, objRestiriction);
		
		long time1 = System.currentTimeMillis();
		Set<OWLClass> targets = cache.check(generateEvent(3));
		System.out.println(System.currentTimeMillis()-time1);

		assertEquals(Collections.singleton(test),targets);
		time1 = System.currentTimeMillis();
		Set<OWLClass> targets2 = cache.check(generateEvent(4));	
		System.out.println(System.currentTimeMillis()-time1);
		assertEquals(true, System.currentTimeMillis()-time1<10);
		assertEquals(Collections.singleton(test),targets2);

	}
	@Test
	public void subClassTest() throws Exception{
		
		File inputOntologyFile = new File("resources/example.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputOntologyFile);
		
		
		LOCERSCache cache = new LOCERSStructureCache();
		cache.init(ontology);
		
		OWLObjectSomeValuesFrom objRestiriction = dataFactory.getOWLObjectSomeValuesFrom(dataFactory.getOWLObjectProperty("http://knowman.idlab.ugent.be/example#hasSupPart"), dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#BackSupport"));
		OWLObjectIntersectionOf objRestiriction2 = dataFactory.getOWLObjectIntersectionOf(dataFactory.getOWLObjectSomeValuesFrom(dataFactory.getOWLObjectProperty("http://knowman.idlab.ugent.be/example#hasSupPart"), dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Leg")),
				dataFactory.getOWLObjectSomeValuesFrom(dataFactory.getOWLObjectProperty("http://knowman.idlab.ugent.be/example#hasSupPart"), dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#BackSupport")));
		OWLClass test = dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Test");
		OWLClass test2 = dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Test2");

		cache.addCQ(test, objRestiriction);
		cache.addCQ(test2, objRestiriction2);
		
		long time1 = System.currentTimeMillis();
		Set<OWLClass> targets = cache.check(generateEvent(2));

		assertEquals(Collections.singleton(test),targets);
		time1 = System.currentTimeMillis();
		Set<OWLClass> targets2 = cache.check(generateEvent2());	
		Set<OWLClass> result = new HashSet<OWLClass>(Collections.singleton(test));
		result.add(test2);
		assertEquals(result,targets2);
		
		Set<OWLClass> targets3 = cache.check(generateEvent(3));
		assertEquals(Collections.singleton(test),targets3);

	}
	@Test
	public void noMatchCacheTest() throws Exception{
		
		File inputOntologyFile = new File("resources/example.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputOntologyFile);
		
		
		LOCERSCache cache = new LOCERSStructureCache();
		cache.init(ontology);
		Set<OWLClass> result = new HashSet<OWLClass>(Collections.singleton(dataFactory.getOWLClass("owl:Nothing")));

		
		long time1 = System.currentTimeMillis();
		Set<OWLClass> targets = cache.check(generateEvent(2));

		assertEquals(result,targets);
		time1 = System.currentTimeMillis();
		Set<OWLClass> targets2 = cache.check(generateEvent2());	
		
		assertEquals(result,targets2);
		time1=System.currentTimeMillis();
		Set<OWLClass> targets3 = cache.check(generateEvent(3));
		assertEquals(true, System.currentTimeMillis()-time1<10);

		assertEquals(result,targets3);

	}
	public Set<OWLAxiom> generateEvent(int i){
		String iri = "http://knowman.idlab.ugent.be/example#";
		Set<OWLAxiom> event = new HashSet<OWLAxiom>();
		event.add(dataFactory.getOWLObjectPropertyAssertionAxiom(dataFactory.getOWLObjectProperty(iri+"hasSupPart"), dataFactory.getOWLNamedIndividual(iri+"chair"+i), dataFactory.getOWLNamedIndividual(iri+"backSupport")));
		
		return event;
	}
	public Set<OWLAxiom> generateEvent2(){
		Set<OWLAxiom> event = generateEvent(2);
		String iri = "http://knowman.idlab.ugent.be/example#";
		event.add(dataFactory.getOWLObjectPropertyAssertionAxiom(dataFactory.getOWLObjectProperty(iri+"hasSupPart"), dataFactory.getOWLNamedIndividual(iri+"chair2"), dataFactory.getOWLNamedIndividual(iri+"leg2")));
		return event;
	}
	public Set<OWLAxiom> generateEventNoHit(){
		Set<OWLAxiom> event = new HashSet<OWLAxiom>();
		String iri = "http://knowman.idlab.ugent.be/example#";
		event.add(dataFactory.getOWLObjectPropertyAssertionAxiom(dataFactory.getOWLObjectProperty(iri+"hasSupPart"), dataFactory.getOWLNamedIndividual(iri+"chair3"), dataFactory.getOWLNamedIndividual(iri+"nothing")));
		return event;
	}
}

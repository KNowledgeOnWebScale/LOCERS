package be.ugent.idlab.loreo.test;

import be.ugent.idlab.loreo.cache.LOREOCache;
import be.ugent.idlab.loreo.cache.LOREONaiveCache;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.Collections;
import java.util.Set;

/**
 * @author pbonte
 *
 */
public class CacheTest {

	@Test
	public void cacheTest() throws Exception{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		File inputOntologyFile = new File("resources/example.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputOntologyFile);
		LOREOCache cache = new LOREONaiveCache();
		cache.init(ontology);
		OWLObjectSomeValuesFrom objRestiriction = dataFactory.getOWLObjectSomeValuesFrom(dataFactory.getOWLObjectProperty("http://knowman.idlab.ugent.be/example#hasObject"), dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Chair"));
		OWLClass test = dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Test");
		OWLClass test2 = dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Test2");

		cache.addCQ(test, objRestiriction);
		cache.addCQ(test2, objRestiriction);
		cache.addCQ(dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Test3"), dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Chair"));
		long time1= System.currentTimeMillis();
		Set<OWLClass> targets = cache.check(Collections.emptySet());
		System.out.println((System.currentTimeMillis()-time1)+" "+targets);
		time1=System.currentTimeMillis();
		System.out.println((System.currentTimeMillis()-time1)+" "+cache.check(Collections.emptySet()));
		time1=System.currentTimeMillis();
		System.out.println((System.currentTimeMillis()-time1)+" "+cache.check(Collections.emptySet()));
	
	}
}

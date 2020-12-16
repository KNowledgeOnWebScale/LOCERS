/**
 * 
 */
package be.ugent.idlab.loreo.test;

import be.ugent.idlab.loreo.Explainer;
import be.ugent.idlab.loreo.query.AxiomIndexer;
import be.ugent.idlab.loreo.query.CacheQuery;
import be.ugent.idlab.loreo.query.CacheQueryGenerator;
import org.junit.Test;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author pbonte
 *
 */
public class ExplainTest {

	@Test
	public void explainTest() throws Exception{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		File inputOntologyFile = new File("resources/example.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputOntologyFile);
		
		Configuration configuration = new Configuration();
		configuration.throwInconsistentOntologyException = false;
		ReasonerFactory factory = new ReasonerFactory();

		// The factory can now be used to obtain an instance of HermiT as an
		// OWLReasoner.
		OWLReasoner reasoner = factory.createReasoner(ontology, configuration);
		
		IRI roomIRI = IRI.create("http://knowman.idlab.ugent.be/example#LivingRoom");
		OWLClass room= dataFactory.getOWLClass(roomIRI);
		OWLIndividual ind = dataFactory.getOWLNamedIndividual(IRI.create("http://knowman.idlab.ugent.be/example#livingRoom"));
		Explainer exp = new Explainer(ontology, reasoner);
		Set<OWLAxiom> explanation = exp.explain(ind, room);
		
		CacheQuery q = CacheQueryGenerator.generate(explanation);
		AxiomIndexer indexer = new AxiomIndexer();
		indexer.addAll(ontology.getAxioms());
		
		boolean found = indexer.checkQuery(q);
		System.out.println(q);
		assertEquals(true, found);
		OWLIndividual ind2 = dataFactory.getOWLNamedIndividual(IRI.create("http://knowman.idlab.ugent.be/example#livingRoom2"));
//
		OWLObjectSomeValuesFrom objRestiriction = dataFactory.getOWLObjectSomeValuesFrom(dataFactory.getOWLObjectProperty("http://knowman.idlab.ugent.be/example#hasObject"), dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Chair"));
		OWLClass test = dataFactory.getOWLClass("http://knowman.idlab.ugent.be/example#Test");
		manager.addAxiom(ontology, dataFactory.getOWLEquivalentClassesAxiom(test,objRestiriction));
		reasoner.flush();
		Set<OWLAxiom> explanation2 =exp.explain(ind2,test);
		System.out.println(explanation2);
		reasoner.instances(objRestiriction).forEach(System.out::println);
		
		CacheQuery q2 = CacheQueryGenerator.generate(explanation2);
		AxiomIndexer indexer2 = new AxiomIndexer();
		indexer2.addAll(ontology.getAxioms());
		
		boolean found2 = indexer2.checkQuery(q2);
		assertEquals(true, found2);
	}
}

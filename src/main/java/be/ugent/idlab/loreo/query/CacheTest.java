package be.ugent.idlab.loreo.query;

import be.ugent.idlab.loreo.query.objects.QueryProperty;
import be.ugent.idlab.loreo.query.objects.QueryType;
import be.ugent.idlab.loreo.query.objects.QueryVal;
import be.ugent.idlab.loreo.query.objects.QueryVar;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.HashSet;
import java.util.Set;

public class CacheTest {
	private static final String ONT_IRI = "http://localhost/SensorRepository.owl#";

	public static void main(String[] args) throws Exception {
		for(int i = 0; i<10;i++){
			Set<OWLAxiom> axioms = generateEvent(i);
			long time1 = System.currentTimeMillis();
			AxiomIndexer indexer = new AxiomIndexer();
			indexer.addAll(axioms);
			System.out.println("AddingTime "+(System.currentTimeMillis()-time1));
			time1=System.currentTimeMillis();
			//indexer.checkQuery(generateNominalQuery(i));
			indexer.checkQuery(generateVarPropQuery());

			System.out.println("CheckTime "+(System.currentTimeMillis()-time1));
		}
		System.out.println("test");
	}

	private static CacheQuery generateQuery(){
		CacheQuery q = new CacheQuery();
		QueryVar x = new QueryVar("x");
		QueryVar y = new QueryVar("y");
		q.add(new QueryType("<"+ONT_IRI+"TestClass>",x))
			.add(new QueryProperty("<"+ONT_IRI+"Prop1>", x, y))
			.add(new QueryType("<"+ONT_IRI+"TestClass2>",y));
		return q;
	}
	private static CacheQuery generateLength2Query(){
		CacheQuery q = new CacheQuery();
		QueryVar x = new QueryVar("x");
		QueryVar y = new QueryVar("y");
		QueryVar z = new QueryVar("z");

		q.add(new QueryType("<"+ONT_IRI+"TestClass>",x))
			.add(new QueryProperty("<"+ONT_IRI+"Prop1>", x, y))
			.add(new QueryProperty("<"+ONT_IRI+"Prop2>", y, z));
		return q;
	}
	private static CacheQuery generateNominalQuery(int i){
		CacheQuery q = new CacheQuery();
		QueryVar x = new QueryVar("x");
		QueryVal y = new QueryVal("<http://localhost/SensorRepository.owl#ind5"+i+">");
		q.add(new QueryType("<"+ONT_IRI+"TestClass>",x))
			.add(new QueryProperty("<"+ONT_IRI+"Prop1>", x, y))
			.add(new QueryType("<"+ONT_IRI+"TestClass2>",y));
		return q;
	}
	private static CacheQuery generateVarPropQuery(){
		CacheQuery q = new CacheQuery();
		QueryVar x = new QueryVar("x");
		QueryVar y = new QueryVar("y");
		q	.add(new QueryProperty("<"+ONT_IRI+"Prop1>", x, y));
		return q;
	}
	private static Set<OWLAxiom> generateEvent(int i){
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory fact = manager.getOWLDataFactory();
		OWLClass cls =  fact.getOWLClass(IRI.create(ONT_IRI+"TestClass"));
		OWLClass cls2 = fact.getOWLClass(IRI.create(ONT_IRI+"TestClass2"));
		
		OWLObjectProperty prop1 = fact.getOWLObjectProperty(IRI.create(ONT_IRI+"Prop1"));
		OWLObjectProperty prop2 = fact.getOWLObjectProperty(IRI.create(ONT_IRI+"Prop2"));
		
		OWLDataProperty dataprop1 = fact.getOWLDataProperty(IRI.create(ONT_IRI+"data1"));
		OWLDataProperty dataprop2 = fact.getOWLDataProperty(IRI.create(ONT_IRI+"data2"));
		
		OWLIndividual ind1 = fact.getOWLNamedIndividual(IRI.create(ONT_IRI+"ind1"+i));
		OWLIndividual ind2 = fact.getOWLNamedIndividual(IRI.create(ONT_IRI+"ind2"+i));
		OWLIndividual ind3 = fact.getOWLNamedIndividual(IRI.create(ONT_IRI+"ind3"+i));
		OWLIndividual ind4 = fact.getOWLNamedIndividual(IRI.create(ONT_IRI+"ind4"+i));
		OWLIndividual ind5 = fact.getOWLNamedIndividual(IRI.create(ONT_IRI+"ind5"+i));

		
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		axioms.add(fact.getOWLClassAssertionAxiom(cls, ind1));
		axioms.add(fact.getOWLClassAssertionAxiom(cls2, ind2));
		axioms.add(fact.getOWLClassAssertionAxiom(cls2, ind3));
		axioms.add(fact.getOWLClassAssertionAxiom(cls2, ind5));

		axioms.add(fact.getOWLClassAssertionAxiom(cls, ind4));

		axioms.add(fact.getOWLObjectPropertyAssertionAxiom(prop1, ind1, ind2));
		axioms.add(fact.getOWLObjectPropertyAssertionAxiom(prop2, ind2, ind3));
		axioms.add(fact.getOWLObjectPropertyAssertionAxiom(prop1, ind4, ind3));
		axioms.add(fact.getOWLObjectPropertyAssertionAxiom(prop1, ind4, ind5));

		
		axioms.add(fact.getOWLDataPropertyAssertionAxiom(dataprop1, ind1, "test"));
		axioms.add(fact.getOWLDataPropertyAssertionAxiom(dataprop2, ind1, "test2"));
		
		return axioms;


	}

}

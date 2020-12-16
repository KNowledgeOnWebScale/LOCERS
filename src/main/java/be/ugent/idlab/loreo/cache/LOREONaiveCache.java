package be.ugent.idlab.loreo.cache;

import be.ugent.idlab.loreo.Explainer;
import be.ugent.idlab.loreo.query.CacheQuery;
import be.ugent.idlab.loreo.query.CacheQueryGenerator;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LOREONaiveCache implements LOREOCache {

	protected OWLOntology ontology;
	private Explainer explainer;
	protected OWLOntologyManager manager;
	protected OWLDataFactory dataFactory;
	protected Set<OWLClass> targets;
	protected OWLReasoner reasoner;
	protected final String EVENT_IRI = "be.idlab.knowmngmt/loreo/Event";
	protected LOREOCacheStructure cacheStruct;

	@Override
	public void init(OWLOntology ontology) {
		this.ontology = ontology;
		this.manager = ontology.getOWLOntologyManager();
		this.dataFactory = manager.getOWLDataFactory();
		Configuration configuration = new Configuration();
		configuration.throwInconsistentOntologyException = false;
		ReasonerFactory factory = new ReasonerFactory();

		this.reasoner = factory.createReasoner(ontology, configuration);
		this.explainer = new Explainer(ontology, reasoner);

		this.targets = new HashSet<OWLClass>();
		this.cacheStruct = new NaiveCacheStructure();
	}
	public void setCacheStructure(NaiveCacheStructure structure){
		this.cacheStruct = structure;
	}

	@Override
	public boolean addCQ(OWLClass target, OWLClassExpression cq) {
		// add to ontology
		manager.addAxiom(ontology, dataFactory.getOWLEquivalentClassesAxiom(target, cq));
		manager.addAxiom(ontology, dataFactory.getOWLSubClassOfAxiom(target, dataFactory.getOWLClass(EVENT_IRI)));
		targets.add(target);
		return false;
	}

	@Override
	public Set<OWLClass> check(Set<OWLAxiom> event) {
		manager.addAxioms(ontology, event);
		// first check the cache
		Set<OWLClass> cachedTargets = cacheStruct.check(ontology.getAxioms());
		if (!cachedTargets.isEmpty()) {
			// results found in cache
			return cachedTargets;
		} else {
			// no results found
			reasoner.flush();
			Set<OWLClass> allResults = new HashSet<OWLClass>();
			// explain results

			for (OWLNamedIndividual in : reasoner.instances(dataFactory.getOWLClass(EVENT_IRI))
					.collect(Collectors.toSet())) {
				Set<OWLClass> results = reasoner.types(in).collect(Collectors.toSet());
				// TODO check if intersections works correctly
				results.retainAll(targets);
				allResults.addAll(results);

				for (OWLClass c : results) {
					Set<OWLAxiom> explanation = explainer.explain(in, c);
					CacheQuery q = CacheQueryGenerator.generate(explanation);
					
					cacheStruct.add(q, Collections.singleton(c));
				}

			}
			return allResults;
		}
	}
	public int getSize(){
		return cacheStruct.getSize();
	}

}

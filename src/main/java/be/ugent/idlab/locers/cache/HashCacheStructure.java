package be.ugent.idlab.locers.cache;

import be.ugent.idlab.locers.query.AxiomIndexer;
import be.ugent.idlab.locers.query.CacheQuery;
import be.ugent.idlab.locers.query.CacheQueryGenerator;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HashCacheStructure extends NaiveCacheStructure {

	private Map<CacheQuery, Set<OWLClass>> queries;
	private Map<Integer, Set<CacheQuery>> hashedQueries;

	public HashCacheStructure() {
		this.queries = new HashMap<CacheQuery, Set<OWLClass>>();
		this.hashedQueries = new HashMap<Integer, Set<CacheQuery>>();
	}

	public Set<OWLClass> check(Set<OWLAxiom> ontology) {
		long timeStart = System.currentTimeMillis();
		long time1 = System.currentTimeMillis();
		CacheQuery newQ = CacheQueryGenerator.generate(ontology);
		Set<CacheQuery> matchingQueries = hashedQueries.get(newQ.generateHashCode());
		System.out.println("hashing: " + (System.currentTimeMillis() - time1));
		time1 = System.currentTimeMillis();
		AxiomIndexer indexer = new AxiomIndexer();
		try {
			indexer.addAll(ontology);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("indexing: " + (System.currentTimeMillis() - time1));
		time1 = System.currentTimeMillis();
		Set<OWLClass> results = new HashSet<OWLClass>();
		if (matchingQueries != null) {
			for (CacheQuery cachedQ : matchingQueries) {
				if (indexer.checkQuery(cachedQ)) {
					if (cachedQ.getNumUsedIndividual() == indexer.getNumberOfIndividuals()) {
						results.addAll(queries.get(cachedQ));
					}
				}
			}
		}
		System.out.println("matching: " + (System.currentTimeMillis() - time1));
		System.out.println("total: " + (System.currentTimeMillis() - timeStart));

		return results;
	}

	public void add(CacheQuery q, Set<OWLClass> targets) {
		queries.put(q, targets);
		if (!hashedQueries.containsKey(q.generateHashCode())) {
			hashedQueries.put(q.generateHashCode(), new HashSet<CacheQuery>());
		}
		hashedQueries.get(q.generateHashCode()).add(q);
	}

	public int getSize() {
		return queries.size();
	}

	public static int generateHashFor(Set<OWLAxiom> axioms) {
		int result = 17;
		int c = 0;
		for (OWLAxiom ax : axioms) {
			if (ax instanceof OWLClassAssertionAxiom) {
				OWLClassAssertionAxiom clAx = (OWLClassAssertionAxiom) ax;
				String clasIRI = clAx.getClassExpression().toString();
				c += 31 * 17 + clasIRI.hashCode();
			} else if (ax instanceof OWLObjectPropertyAssertionAxiom) {
				OWLObjectPropertyAssertionAxiom objAx = (OWLObjectPropertyAssertionAxiom) ax;
				String propIRI = objAx.getProperty().toString();
				c += 31 * 17 + propIRI.hashCode();
			}
		}
		return 31 * result + c;
	}

}

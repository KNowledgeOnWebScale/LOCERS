package be.ugent.idlab.locers.cache;

import be.ugent.idlab.locers.query.AxiomIndexer;
import be.ugent.idlab.locers.query.CacheQuery;
import be.ugent.idlab.locers.query.CacheQueryGenerator;
import org.semanticweb.owlapi.model.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HashMaterializedCacheStructure extends MaterializeCacheStructure {

	private Map<CacheQuery, Map<String,Set<OWLClass>>> queries;
	private Map<Integer, Set<CacheQuery>> hashedQueries;
	private OWLOntology tempOnt;
	public HashMaterializedCacheStructure() {
		this.queries = new HashMap<CacheQuery, Map<String,Set<OWLClass>>>();
		this.hashedQueries = new HashMap<Integer, Set<CacheQuery>>();
		try {
			this.tempOnt = manager.createOntology();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}

	public Set<OWLAxiom> checkMaterialized(Set<OWLAxiom> ontology) {
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
		Set<OWLAxiom> results = new HashSet<OWLAxiom>();
		if (matchingQueries != null) {
			for (CacheQuery cachedQ : matchingQueries) {
				if (indexer.checkQuery(cachedQ)) {
					if (cachedQ.getNumUsedIndividual() == indexer.getNumberOfIndividuals()) {
						System.out.println("found match: " + (System.currentTimeMillis() - time1));
						Map<String,String> indVarMap = individualVariableExtractor(cachedQ);
						System.out.println("create ind variable map: " + (System.currentTimeMillis() - time1));

							manager.addAxioms(tempOnt,ontology);
							System.out.println("create ontology: " + (System.currentTimeMillis() - time1));
							for(OWLNamedIndividual in:tempOnt.individualsInSignature().collect(Collectors.toSet())){

								String indStr = in.toString();
								if(indVarMap.containsKey(indStr)){
									String varName = indVarMap.get(indStr);
									for(OWLClass classAx:queries.get(cachedQ).get(varName)){
										results.add(dataFact.getOWLClassAssertionAxiom(classAx,in));
									}

								}

							}
						manager.removeAxioms(tempOnt,ontology);
					}
				}
			}
		}
		System.out.println("matching: " + (System.currentTimeMillis() - time1));
		System.out.println("total: " + (System.currentTimeMillis() - timeStart));

		return results;
	}

	public void add(CacheQuery q, Map<String,Set<OWLClass>> targets) {

	}
	public void addMaterialize(CacheQuery q, Map<String,Set<OWLClass>> targets){
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

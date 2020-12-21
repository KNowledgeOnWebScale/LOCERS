package be.ugent.idlab.locers.cache;

import be.ugent.idlab.locers.query.AxiomIndexer;
import be.ugent.idlab.locers.query.CacheQuery;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;
import java.util.Map.Entry;

public class NaiveCacheStructure implements LOCERSCacheStructure {
	
	private Map<CacheQuery,Set<OWLClass>> queries;
	private OWLDataFactory dataFact = new OWLDataFactoryImpl();

	public NaiveCacheStructure(){
		this.queries = new HashMap<CacheQuery,Set<OWLClass>>();
	}

	@Override
	public Set<OWLClass> check(Set<OWLAxiom> ontology){
		AxiomIndexer indexer = new AxiomIndexer();
		try {
			indexer.addAll(ontology);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set<OWLClass> results = new HashSet<OWLClass>();
		for(Entry<CacheQuery,Set<OWLClass>> ent : queries.entrySet()){
			if(indexer.checkQuery(ent.getKey())){
				if(ent.getKey().getNumUsedIndividual()==indexer.getNumberOfIndividuals()){
					results.addAll(ent.getValue());
				}
			}
		}
		return results;
	}
	public Set<OWLAxiom> checkMaterialized(Set<OWLAxiom> ontology){

		return Collections.emptySet();
	}
	public void add(CacheQuery q, Set<OWLClass> targets){
		queries.put(q, targets);
	}
	public void addMaterialize(CacheQuery q,Map<String,Set<OWLClass>> targets){

	}
	public int getSize(){
		return queries.size();
	}

}

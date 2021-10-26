package be.ugent.idlab.locers.cache;

import be.ugent.idlab.locers.cache.utils.CacheStrategyInf;
import be.ugent.idlab.locers.cache.utils.GreedyCacheStrategy;
import be.ugent.idlab.locers.query.AxiomIndexer;
import be.ugent.idlab.locers.query.CacheQuery;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MaterializeCacheStructure implements LOCERSCacheStructure {

	protected OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	private CacheStrategyInf<CacheQuery,Map<String,Set<OWLClass>>> cacheStrategyInf;
	public MaterializeCacheStructure(){
		this.cacheStrategyInf = new GreedyCacheStrategy<>();
	}
	public MaterializeCacheStructure(CacheStrategyInf newStrategy){
		this.cacheStrategyInf = newStrategy;
	}
	public void setCacheStrategy(CacheStrategyInf newStrategy){
		this.cacheStrategyInf = newStrategy;
	}
	protected OWLDataFactory dataFact = new OWLDataFactoryImpl();

	public Set<OWLAxiom> checkMaterialized(Set<OWLAxiom> ontology){
		AxiomIndexer indexer = new AxiomIndexer();
		try {
			indexer.addAll(ontology);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Set<OWLAxiom> results = new HashSet<OWLAxiom>();
		for(Entry<CacheQuery,Map<String,Set<OWLClass>>> ent : cacheStrategyInf.getData().entrySet()){
			if(indexer.checkQuery(ent.getKey())){
				CacheQuery query = ent.getKey();
				if(query.getNumUsedIndividual()==indexer.getNumberOfIndividuals()){
					//mark cache hit for cache strategy replacement (if any)
					cacheStrategyInf.reference(query);
					Map<String,String> indVarMap = individualVariableExtractor(query);
					try {
						OWLOntology tempOnt = manager.createOntology(ontology.stream());

						for(OWLNamedIndividual in:tempOnt.individualsInSignature().collect(Collectors.toSet())){

								String indStr = in.toString();

								if(indVarMap.containsKey(indStr)){
									String varName = indVarMap.get(indStr);
									for(OWLClass classAx:ent.getValue().get(varName)){
										results.add(dataFact.getOWLClassAssertionAxiom(classAx,in));
									}

								}
						}
						manager.removeOntology(tempOnt);
					} catch (OWLOntologyCreationException e) {
						e.printStackTrace();
					}

				}
			}
		}
		return results;
	}
	protected Map<String,String> individualVariableExtractor(CacheQuery q){
		Map<String, String> swapped = q.getBindings().entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		return swapped;
	}
	public Set<OWLClass> check(Set<OWLAxiom> ontology){
		return Collections.emptySet();
	}
	public void add(CacheQuery q, Set<OWLClass> targets){

	}
	public void addMaterialize(CacheQuery q, Map<String,Set<OWLClass>> targets){

		cacheStrategyInf.add(q,targets);
	}
	public int getSize(){
		return cacheStrategyInf.getData().size();
	}

}

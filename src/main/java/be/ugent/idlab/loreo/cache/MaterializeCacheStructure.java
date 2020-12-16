package be.ugent.idlab.loreo.cache;

import be.ugent.idlab.loreo.query.AxiomIndexer;
import be.ugent.idlab.loreo.query.CacheQuery;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MaterializeCacheStructure implements LOREOCacheStructure{

	private Map<CacheQuery,Map<String,Set<OWLClass>>> queries;
	protected OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	public MaterializeCacheStructure(){
		this.queries = new HashMap<CacheQuery,Map<String,Set<OWLClass>>>();
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
		for(Entry<CacheQuery,Map<String,Set<OWLClass>>> ent : queries.entrySet()){
			if(indexer.checkQuery(ent.getKey())){
				CacheQuery query = ent.getKey();
				if(query.getNumUsedIndividual()==indexer.getNumberOfIndividuals()){
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
		queries.put(q, targets);
	}
	public int getSize(){
		return queries.size();
	}

}

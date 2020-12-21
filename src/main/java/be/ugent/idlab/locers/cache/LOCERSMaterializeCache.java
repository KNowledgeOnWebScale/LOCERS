/**
 * 
 */
package be.ugent.idlab.locers.cache;

import be.ugent.idlab.locers.query.CacheQuery;
import be.ugent.idlab.locers.query.CacheQueryGenerator2;
import be.ugent.idlab.locers.query.DataConstraint;
import be.ugent.idlab.locers.query.objects.QueryVar;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author pbonte
 *
 */
public class LOCERSMaterializeCache {
	protected OWLOntology ontology;
	protected OWLOntologyManager manager;
	protected OWLDataFactory dataFactory;
	protected Set<OWLClass> targets;
	protected OWLReasoner reasoner;
	protected final String EVENT_IRI = "be.idlab.knowmngmt/loreo/Event";
	protected MaterializeCacheStructure cacheStruct;
	protected Map<String, DataConstraint> dataClsMap;
	private Set<OWLNamedIndividual> statics;


	public void init(OWLOntology ontology) {
		this.ontology = ontology;
		this.manager = ontology.getOWLOntologyManager();
		this.dataFactory = manager.getOWLDataFactory();
		Configuration configuration = new Configuration();
		configuration.throwInconsistentOntologyException = false;
		configuration.ignoreUnsupportedDatatypes=true;
		Reasoner.ReasonerFactory factory = new Reasoner.ReasonerFactory();

		this.reasoner = factory.createReasoner(ontology, configuration);

		this.targets = new HashSet<OWLClass>();
		this.cacheStruct = new MaterializeCacheStructure();
		dataClsMap = new HashMap<String,DataConstraint>();
		findDataRestrictions();
		this.statics = ontology.individualsInSignature().collect(Collectors.toSet());


	}
	public void addToStatic(Set<OWLNamedIndividual> staticInds){
		this.statics.addAll(staticInds);
	}
	private void findDataRestrictions(){
		for(OWLClass cls:ontology.getClassesInSignature()){
			if(ontology.getEquivalentClassesAxioms(cls).toString().contains("DataSomeValuesFrom")){
				for(OWLEquivalentClassesAxiom eqAx :ontology.getEquivalentClassesAxioms(cls)){
					DataRestrictionVisitor r = new DataRestrictionVisitor();
					eqAx.classExpressions().forEach(v -> v.accept(r));
					dataClsMap.put(cls.toStringID(),new DataConstraint(r.getDataProp(),r.getValue(), r.getRestriction()));
				}
			}
		}
	}

	public Set<OWLAxiom> check(Set<OWLAxiom> event) {
		OWLOntology tempOnt;
		try {
			tempOnt = manager.createOntology();

			manager.addAxioms(tempOnt, event);
			// first check the cache
			Set<OWLAxiom> extended = tempOnt.individualsInSignature().map(ind -> ontology.classAssertionAxioms(ind))
					.flatMap(Function.identity()).collect(Collectors.toSet());
			
			extended.addAll(event);
			long time0 = System.currentTimeMillis();
			Set<OWLAxiom> cachedTargets = cacheStruct.checkMaterialized(extended);
			System.out.println("Matching time: " + (System.currentTimeMillis()-time0));
			if (!cachedTargets.isEmpty()) {
				// results found in cache
				System.out.println("Cache hit");
				return cachedTargets;
			} else {
				System.out.println("Cache miss");
				Set<OWLAxiom> allResults = new HashSet<OWLAxiom>();
				manager.addAxioms(ontology, event);

				// no results found
				reasoner.flush();
				// explain results
				// extends the event with additional type info from the static
				// ontology
				Map<String, QueryVar> varNames = new HashMap<String,QueryVar>();

				Map<String,Set<OWLClass>> individualTypes = new HashMap<String,Set<OWLClass>>();
				Map<String,DataConstraint> dataConstraints = new HashMap<String,DataConstraint>();
				CacheQuery q = CacheQueryGenerator2.generate(extended,varNames,statics);
				for (OWLNamedIndividual in : tempOnt.individualsInSignature().collect(Collectors.toSet())) {
						Set<OWLClass> results = reasoner.types(in).collect(Collectors.toSet());
						if(varNames.containsKey(in.getIRI().toString())){
							//query variables
							individualTypes.put(varNames.get(in.getIRI().toString()).getName(),results);
						}else{
							//query values
							individualTypes.put(in.getIRI().toString(),results);
						}

						for(OWLClass result: results){
							allResults.add(dataFactory.getOWLClassAssertionAxiom(result,in));
							//find data property restrictions
							if(dataClsMap.containsKey(result.toStringID())){
								DataConstraint dataCont = dataClsMap.get(result.toStringID());
								dataConstraints.put(dataCont.getDataProp(),new DataConstraint(dataCont.getDataProp(),in.toStringID(),dataCont.getConstraintValue(),dataCont.getConstraint()));
							}
						}

				}
				CacheQueryGenerator2.generateDataProps(q,extended,varNames,statics,dataConstraints);

				cacheStruct.addMaterialize(q,individualTypes);
				/*if(allResults.isEmpty()){
					CacheQuery q = CacheQueryGenerator.generate(extended);
					cacheStruct.add(q, Collections.singleton(dataFactory.getOWLClass("owl:Nothing")));
					allResults.add(dataFactory.getOWLClass("owl:Nothing"));
				}*/
				manager.removeAxioms(ontology, event);
				return allResults;
			}
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Collections.emptySet();
	}

	public void setCacheStructure(MaterializeCacheStructure structure){
		this.cacheStruct = structure;
	}

	public LOCERSCacheStructure getCacheStructure(){
		return cacheStruct;
	}
	public static void saveOntology(OWLOntology ontology, String name) {
		if (ontology != null) {
			String location = "/tmp/loreo/";
			try {
				File file = new File(location + name);
				if (!file.canExecute()) {
					File mkdir = new File(location);
					mkdir.mkdirs();
				}
				file.createNewFile();
				ontology.getOWLOntologyManager().saveOntology(ontology, new TurtleDocumentFormat(),
						new FileOutputStream(file));
			} catch (OWLOntologyStorageException | IOException e) {
				e.printStackTrace();
			}
		}

	}
	public int getSize(){
		return cacheStruct.getSize();
	}
}

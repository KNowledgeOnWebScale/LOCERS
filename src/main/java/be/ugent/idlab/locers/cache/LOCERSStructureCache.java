/**
 * 
 */
package be.ugent.idlab.locers.cache;

import be.ugent.idlab.locers.query.CacheQuery;
import be.ugent.idlab.locers.query.CacheQueryGenerator;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author pbonte
 *
 */
public class LOCERSStructureCache extends LOCERSNaiveCache {

	@Override
	public Set<OWLClass> check(Set<OWLAxiom> event) {
		OWLOntology tempOnt;
		try {
			tempOnt = manager.createOntology();

			manager.addAxioms(tempOnt, event);
			// first check the cache
			Set<OWLAxiom> extended = tempOnt.individualsInSignature().map(ind -> ontology.classAssertionAxioms(ind))
					.flatMap(Function.identity()).collect(Collectors.toSet());
			
			extended.addAll(event);
			Set<OWLClass> cachedTargets = cacheStruct.check(extended);
			if (!cachedTargets.isEmpty()) {
				// results found in cache
				System.out.println("Cache hit");
				return cachedTargets;
			} else {
				System.out.println("Cache miss");
				Set<OWLClass> allResults = new HashSet<OWLClass>();
				manager.addAxioms(ontology, event);

				// no results found
				reasoner.flush();
				// explain results
				// extends the event with additional type info from the static
				// ontology

				for (OWLNamedIndividual in : reasoner.instances(dataFactory.getOWLClass(EVENT_IRI))
						.collect(Collectors.toSet())) {
					if (tempOnt.containsIndividualInSignature(in.getIRI())) {
						Set<OWLClass> results = reasoner.types(in).collect(Collectors.toSet());
						results.retainAll(targets);
						allResults.addAll(results);
						for (OWLClass c : results) {
							CacheQuery q = CacheQueryGenerator.generate(extended);
							cacheStruct.add(q, Collections.singleton(c));
						}
					}

				}
				if(allResults.isEmpty()){
					CacheQuery q = CacheQueryGenerator.generate(extended);
					cacheStruct.add(q, Collections.singleton(dataFactory.getOWLClass("owl:Nothing")));
					allResults.add(dataFactory.getOWLClass("owl:Nothing"));
				}
				manager.removeAxioms(ontology, event);
				return allResults;
			}
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Collections.emptySet();
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
}

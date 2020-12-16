/**
 * 
 */
package be.ugent.idlab.loreo.examples;

import be.ugent.idlab.loreo.cache.LOREOMaterializeCache;
import be.ugent.idlab.loreo.cache.LOREOStructureCache;
import be.ugent.idlab.loreo.cache.MaterializeCacheStructure;
import be.ugent.idlab.loreo.query.CacheQuery;
import be.ugent.idlab.loreo.query.CacheQueryGenerator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author pbonte
 *
 */
public class CityBenchPlusTest {

	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLDataFactory dataFactory = manager.getOWLDataFactory();
	static final String mapping =
			"@prefix xsd: <http://www.w3.org/2001/XMLSchema#>. \n" +
					"@prefix : <http://massif/>. \n" +
					"@prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#>. \n" +
					"@prefix ct: <http://www.insight-centre.org/citytraffic#>.\n" +
					"@prefix ses: <http://www.insight-centre.org/dataset/SampleEventService#>.\n"+
					"@prefix mas: <http://massif.streaming/ontologies/rsplab/officerepository.owl#>. \n"
					+ "ct:hasValue rdf:type owl:DatatypeProperty .\n" +
					"ssn:observedProperty rdf:type owl:ObjectProperty .\n" +
					"ssn:observedBy rdf:type owl:ObjectProperty .\n" +
					"mas:hasDiscreteValue rdf:type owl:ObjectProperty .\n" +
					":?_id a ssn:Observation ; :eventTime \"?TIMESTAMP\"^^xsd:dateTime ; ct:hasValue \"?vehicleCount\"^^xsd:int; mas:hasDiscreteValue ?discreteVehicleCount ; ssn:observedProperty ses:vehicleCount ; ssn:observedBy ses:AarhusTrafficData186979 .\n"
					+ "ses:vehicleCount a ct:CongestionLevel. ";

	public static void main(String[] args) throws Exception {


		OWLOntologyManager manager = OWLManager.createConcurrentOWLOntologyManager();


		String path = "/Users/psbonte/Documents/Documents/TestWorkspace/CityBenchPlus/resource/";
		if(args.length >= 1) {
			path = args[0];
		}		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(path + "officerepo.owl"));

		SimpleMapper mapper = new SimpleMapper(mapping, true);
		mapper.registerFunction("discreteVehicleCount", "vehicleCount", new MappingFunction() {

			@Override
			public String apply(String input) {
				int parsedInt = Integer.parseInt(input);
				if (parsedInt < 5) {
					return "mas:lowValue";
				} else if (parsedInt < 15) {
					return "mas:mediumValue";
				} else if (parsedInt >= 15) {
					return "mas:highValue";
				}
				return null;
			}

		});
		String fileName = path+"/AarhusTrafficData182955.stream";
		LOREOMaterializeCache cache = new LOREOMaterializeCache();
		cache.init(ontology);
		cache.setCacheStructure(new MaterializeCacheStructure());

		// read file into stream, try-with-resources
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

			stream.map(p -> parseEvent(cache,manager,mapper.map(p),ontology)).limit(100).forEach(System.out::println);

		} catch (IOException e) {
			e.printStackTrace();
		}





	}
	public static Set<OWLAxiom> parseEvent(LOREOMaterializeCache cache,OWLOntologyManager manager, String triples,OWLOntology ont) {
		if(triples==null) {
			return Collections.emptySet();
		}
		// load event
		OWLOntology eventOnt;
		try {
			OWLDataFactory fact = manager.getOWLDataFactory();
			eventOnt = manager.loadOntologyFromOntologyDocument(new StringDocumentSource(triples));
			Set<OWLAxiom> event = eventOnt.axioms().filter(ax->!ax.toString().contains("Declaration")).collect(Collectors.toSet());
			Set<OWLIndividual> inds = event.stream().filter(a->a instanceof OWLClassAssertionAxiom).map(a -> ((OWLClassAssertionAxiom)a).getIndividual()).collect(Collectors.toSet());
			//add axioms
			long time1 = System.currentTimeMillis();

			Set<OWLAxiom> result2 = cache.check(event);
			//manager.saveOntology(ont, new TurtleDocumentFormat(),IRI.create(new File("test.owl").toURI()));

			//remove axioms
			manager.removeAxioms(ont, event);
			long finalTime = System.currentTimeMillis() - time1;
			System.out.println("Time:\t"+finalTime);
			System.out.println(result2);
			System.out.println("Size:\t"+result2.size());
			return result2;


		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//return Collections.emptySet();
		}
		return Collections.emptySet();

	}
	private static Set<OWLAxiom> substituteStudent(Set<OWLAxiom> studentAxs) {
		final OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology o;
		try {
			o = m.createOntology(studentAxs);

			OWLDataFactory fact = m.getOWLDataFactory();
			final OWLEntityRenamer renamer = new OWLEntityRenamer(m, Collections.singleton(o));
			final Map<OWLEntity, IRI> entity2IRIMap = new HashMap<>();
			//find class assertion
			UUID uuid = UUID.randomUUID();

			for(OWLAxiom ax: studentAxs) {
				if(ax instanceof OWLClassAssertionAxiom) {
					OWLClassAssertionAxiom clsAx = (OWLClassAssertionAxiom)ax;
					IRI iri = clsAx.getIndividual().asOWLNamedIndividual().getIRI();
					entity2IRIMap.put(clsAx.getIndividual().asOWLNamedIndividual(),IRI.create(iri.toString()+"_"+uuid.toString()));
				}
			}
			o.applyChanges(renamer.changeIRI(entity2IRIMap));
			return o.getAxioms();
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Collections.EMPTY_SET;
	}
	public static Set<OWLAxiom> generateStudent(int id,OWLDataFactory factory){
		String iriGradStud = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#UndergraduateStudent";
		String iriMemberOf = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#memberOf";
		String iriDepartment = "http://www.Department2.University1.edu";
		
		OWLClass gradStudClass = factory.getOWLClass(iriGradStud);
		OWLObjectProperty memberOfProp = factory.getOWLObjectProperty(iriMemberOf);
		OWLNamedIndividual departmentInd = factory.getOWLNamedIndividual(iriDepartment);
		
		Set<OWLAxiom> event = new HashSet<OWLAxiom>();
		OWLNamedIndividual newStud = factory.getOWLNamedIndividual(iriGradStud+"_"+id);
		event.add(factory.getOWLClassAssertionAxiom(gradStudClass, newStud));
		event.add(factory.getOWLObjectPropertyAssertionAxiom(memberOfProp, newStud, departmentInd));
		
		return event;
	}
	public static Set<OWLAxiom> generateStudentMultiple(int number,int start,OWLDataFactory factory){
		String iriGradStud = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#UndergraduateStudent";
		String iriMemberOf = "http://swat.cse.lehigh.edu/onto/univ-bench.owl#memberOf";
		String iriDepartment = "http://www.Department2.University1.edu";
		String hasValue = "http://example.org/lubm.owl#hasValue";
		Set<OWLAxiom> event = new HashSet<OWLAxiom>();
		OWLClass gradStudClass = factory.getOWLClass(iriGradStud);
		OWLObjectProperty memberOfProp = factory.getOWLObjectProperty(iriMemberOf);
		OWLNamedIndividual departmentInd = factory.getOWLNamedIndividual(iriDepartment);
		OWLDataProperty hasValueProp = factory.getOWLDataProperty(hasValue);
		
		for(int i = 0 ; i <number;i++){
			
		OWLNamedIndividual newStud = factory.getOWLNamedIndividual(iriGradStud+"Test_"+i+start);
		event.add(factory.getOWLClassAssertionAxiom(gradStudClass, newStud));
		event.add(factory.getOWLObjectPropertyAssertionAxiom(memberOfProp, newStud, departmentInd));
		event.add(factory.getOWLDataPropertyAssertionAxiom(hasValueProp, newStud, 1+start));

		}
		
		return event;
	}
	public static void adaptTboxForTest(int numTests, OWLOntology ontology) {
		String iri = "http://knowman.idlab.ugent.be/example#";
		for (int j = 0; j < numTests; j++) {
			manager.addAxiom(ontology,
					dataFactory.getOWLDeclarationAxiom(dataFactory.getOWLObjectProperty(iri + "hasSupPart" + j)));
		}
	}

	public static void adaptTboxForTestExtending(int numTests, int numRelations, OWLOntology ontology) {
		String iri = "http://knowman.idlab.ugent.be/example#";
		for (int j = 0; j < numRelations; j++) {
			manager.addAxiom(ontology, dataFactory
					.getOWLDeclarationAxiom(dataFactory.getOWLObjectProperty(iri + "hasSupPart" + numTests + "_" + j)));
		}
	}

	public static Set<OWLAxiom> generateEventExtending(int i, int numRelations) {
		String iri = "http://knowman.idlab.ugent.be/example#";
		Set<OWLAxiom> event = new HashSet<OWLAxiom>();
		event.add(dataFactory.getOWLObjectPropertyAssertionAxiom(dataFactory.getOWLObjectProperty(iri + "hasSupPart"),
				dataFactory.getOWLNamedIndividual(iri + "chair2"),
				dataFactory.getOWLNamedIndividual(iri + "backSupport")));
		for (int j = 0; j < numRelations; j++) {
			event.add(dataFactory.getOWLObjectPropertyAssertionAxiom(
					dataFactory.getOWLObjectProperty(iri + "hasSupPart" + i + "_" + j),
					dataFactory.getOWLNamedIndividual(iri + "chair2"),
					dataFactory.getOWLNamedIndividual(iri + "backSupport" + i + "_" + j)));

		}
		return event;
	}

	public static Set<OWLAxiom> generateEvent(int i) {
		String iri = "http://knowman.idlab.ugent.be/example#";
		Set<OWLAxiom> event = new HashSet<OWLAxiom>();
		event.add(dataFactory.getOWLObjectPropertyAssertionAxiom(dataFactory.getOWLObjectProperty(iri + "hasSupPart"),
				dataFactory.getOWLNamedIndividual(iri + "chair2"),
				dataFactory.getOWLNamedIndividual(iri + "backSupport")));
		int j = i;
		event.add(
				dataFactory.getOWLObjectPropertyAssertionAxiom(dataFactory.getOWLObjectProperty(iri + "hasSupPart" + j),
						dataFactory.getOWLNamedIndividual(iri + "chair2"),
						dataFactory.getOWLNamedIndividual(iri + "backSupport" + j)));

		return event;
	}

	public static Set<OWLAxiom> generateEvent2() {
		Set<OWLAxiom> event = generateEvent(2);
		String iri = "http://knowman.idlab.ugent.be/example#";
		event.add(dataFactory.getOWLObjectPropertyAssertionAxiom(dataFactory.getOWLObjectProperty(iri + "hasSupPart"),
				dataFactory.getOWLNamedIndividual(iri + "chair2"), dataFactory.getOWLNamedIndividual(iri + "leg2")));
		return event;
	}

	public static void pupulateCache(LOREOStructureCache cache, OWLOntology ontology, Set<OWLAxiom> event) {
		OWLOntology tempOnt;
		try {
			tempOnt = manager.createOntology();
			manager.addAxioms(tempOnt, event);
			Set<OWLAxiom> extended = tempOnt.individualsInSignature().map(ind -> ontology.classAssertionAxioms(ind))
					.flatMap(Function.identity()).collect(Collectors.toSet());

			extended.addAll(event);

			CacheQuery q = CacheQueryGenerator.generate(extended);
			cache.getCacheStructure().add(q, Collections.singleton(dataFactory.getOWLClass("owl:Thing")));
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

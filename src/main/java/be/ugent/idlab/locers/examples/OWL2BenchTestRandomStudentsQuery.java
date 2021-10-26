/**
 * 
 */
package be.ugent.idlab.locers.examples;

import be.ugent.idlab.locers.cache.LOCERSMaterializeCache;
import be.ugent.idlab.locers.cache.LOCERSStructureCache;
import be.ugent.idlab.locers.cache.MaterializeCacheStructure;
import be.ugent.idlab.locers.query.CacheQuery;
import be.ugent.idlab.locers.query.CacheQueryGenerator;
import be.ugent.idlab.locers.query.utils.OWLJenaUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;



public class OWL2BenchTestRandomStudentsQuery {

	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	static OWLDataFactory dataFactory = manager.getOWLDataFactory();

	public static void main(String[] args) throws Exception {

		String path="resources/";
		 path="/Users/psbonte/Documents/Documents/TestWorkspace/OWL2BenchStream/resource/OWL2Bench/";

		int num_students = 10;
		if(args.length >= 2) {
			path = args[0];
			num_students = Integer.parseInt(args[1]);
		}
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(path + "noStudents.owl"));
		OWLOntologyManager managerMat = OWLManager.createOWLOntologyManager();
		OWLOntology mat = managerMat.loadOntologyFromOntologyDocument(new File(path + "noStudents_mat2.owl"));

		LOCERSMaterializeCache cache = new LOCERSMaterializeCache();
		cache.init(ontology);
		cache.setCacheStructure(new MaterializeCacheStructure());

		List<Set<OWLAxiom>> studentList = new ArrayList<Set<OWLAxiom>>();
		for(int i = 0 ; i < num_students;i++) {
			OWLOntology stud = manager.loadOntologyFromOntologyDocument(new File(path + "student_"+i+".owl"));
			studentList.add(stud.axioms().filter(a->!(a  instanceof OWLDeclarationAxiom)).collect(Collectors.toSet()));
		}

		List<Query> queries  = generateQueries();

		int streamLength = 100;
		for(int i = 0 ; i <streamLength;i++) {
			int randomIndex = (int)(Math.random() * (studentList.size()  ));
			Set<OWLAxiom> studentAx = studentList.get(i%studentList.size());
				Set<OWLAxiom> subStudent = substituteStudent(studentAx);
				Set<OWLIndividual> inds = subStudent.stream().filter(a->a instanceof OWLClassAssertionAxiom).map(a -> ((OWLClassAssertionAxiom)a).getIndividual()).collect(Collectors.toSet());
			System.out.println(inds.size());
				//add axioms
				Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>();
				allAxioms.addAll(mat.getAxioms());
				Model model = OWLJenaUtils.getOntologyModel(managerMat, allAxioms);

				long time1 = System.currentTimeMillis();
				Set<OWLAxiom> result2 = cache.check(subStudent);



				Model event = OWLJenaUtils.getOntologyModel(manager, result2);
				model.add(event);

				//query
				String totalResults = "";

				for(Query q:queries) {
					Model results = OWLJenaUtils.queryConstruct(q, model);
					totalResults +=results.size() +", ";
				}

				//remove axioms
				long finalTime = System.currentTimeMillis() - time1;
				System.out.println("Time:\t"+finalTime);
				System.out.println("Size:\t"+totalResults);
				System.out.println("Cache size:\t" + cache.getSize());

		}


	}
	public static List<Query> generateQueries(){
		String onto = "/Users/psbonte/Documents/Documents/TestWorkspace/OWL2BenchStream/resource/OWL2Bench/noStudents2.owl";
		ArrayList<String> queries = new ArrayList<String>();

		// <http://benchmark/OWL2Bench#>
		queries.add("PREFIX : <http://benchmark/OWL2Bench#>\n CONSTRUCT {?x  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  :T20CricketFan} WHERE{?x  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  :T20CricketFan}");
		queries.add("PREFIX : <http://benchmark/OWL2Bench#>\n CONSTRUCT {?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> :SelfAwarePerson} WHERE{?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> :SelfAwarePerson}");
		queries.add("PREFIX : <http://benchmark/OWL2Bench#>\n CONSTRUCT {?x  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  :Person} WHERE{?x  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  :Person}");
		queries.add("PREFIX : <http://benchmark/OWL2Bench#>\n CONSTRUCT {?x  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  :LeisureStudent} WHERE{?x  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  :LeisureStudent}");
//		queries.add("PREFIX : <http://benchmark/OWL2Bench#>\n CONSTRUCT {?s  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  :Student.} WHERE{?s  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  :Student.  ?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  :Organization.  ?x  :hasDean  ?z.  ?z  :teachesCourse  ?c.  ?s :takesCourse  ?c}");
		queries.add("PREFIX : <http://benchmark/OWL2Bench#>\n CONSTRUCT {?x  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  :UGStudent} WHERE{?x  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  :UGStudent}");
		queries.add("PREFIX : <http://benchmark/OWL2Bench#>\n CONSTRUCT {?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> :PeopleWithManyHobbies} WHERE{?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> :PeopleWithManyHobbies}");
//		queries.add("PREFIX : <http://benchmark/OWL2Bench#>\n CONSTRUCT {?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> :Student.} WHERE{?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> :Student. ?x :isStudentOf ?y. ?y :isPartOf ?z . ?z :hasCollegeDiscipline :Engineering}");

		List<Query> newQueries = new ArrayList<>();
		for (int i = 0 ; i < queries.size(); i++) {
			String queryfile = queries.get(i);
			Query query = QueryFactory.create(queryfile);


			newQueries.add(query);

		}
		return newQueries;
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

	public static void pupulateCache(LOCERSStructureCache cache, OWLOntology ontology, Set<OWLAxiom> event) {
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

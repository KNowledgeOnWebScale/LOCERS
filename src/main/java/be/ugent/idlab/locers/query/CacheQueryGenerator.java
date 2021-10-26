/**
 * 
 */
package be.ugent.idlab.locers.query;

import be.ugent.idlab.locers.query.objects.QueryProperty;
import be.ugent.idlab.locers.query.objects.QueryType;
import be.ugent.idlab.locers.query.objects.QueryVar;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CacheQueryGenerator {

	public static CacheQuery generate(Set<OWLAxiom> axioms){
		Map<String,QueryVar> varNames = new HashMap<String,QueryVar>();
		return generate(axioms,varNames);
	}
	public static CacheQuery generate(Set<OWLAxiom> axioms,Map<String,QueryVar> varNames){
		char varName = 'a';
		CacheQuery q = new CacheQuery();
		
		for(OWLAxiom ax: axioms){
			if(ax instanceof OWLClassAssertionAxiom){
				OWLClassAssertionAxiom clasAss = (OWLClassAssertionAxiom)ax;
				OWLIndividual ind = clasAss.getIndividual();
				if(!varNames.containsKey(ind.toStringID())){
					varNames.put(ind.toStringID(), new QueryVar(""+varName++));
				}
				String clasIRI = clasAss.getClassExpression().toString();
				q.add(new QueryType(clasIRI, varNames.get(ind.toStringID())));
			}
			if(ax instanceof OWLObjectPropertyAssertionAxiom){
				OWLObjectPropertyAssertionAxiom objAx = (OWLObjectPropertyAssertionAxiom)ax;
				OWLIndividual subj = objAx.getSubject();
				OWLIndividual obj = objAx.getObject();
				String propIRI = objAx.getProperty().toString();
				if(!varNames.containsKey(subj.toStringID())){
					varNames.put(subj.toStringID(), new QueryVar(""+varName++));
				}
				if(!varNames.containsKey(obj.toStringID())){
					varNames.put(obj.toStringID(), new QueryVar(""+varName++));
				}
				q.add(new QueryProperty(propIRI, varNames.get(subj.toStringID()), varNames.get(obj.toStringID())));
			}
		}
		q.setExpectedNumIndividuals(varNames.size());
		return q;
	}
	
	private static Map<String,String> generateVariables(Set<OWLAxiom> axioms){
		return null;
	}
	

}

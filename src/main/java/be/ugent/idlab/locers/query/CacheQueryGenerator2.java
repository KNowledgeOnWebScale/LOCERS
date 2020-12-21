/**
 * 
 */
package be.ugent.idlab.locers.query;

import be.ugent.idlab.locers.query.objects.*;
import org.semanticweb.owlapi.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author pbonte
 *
 */
public class CacheQueryGenerator2 {

	public static CacheQuery generate(Set<OWLAxiom> axioms, Set<OWLNamedIndividual> statics){
		Map<String,QueryVar> varNames = new HashMap<String,QueryVar>();
		return generate(axioms,varNames,statics);
	}


	public static CacheQuery generate(Set<OWLAxiom> axioms,Map<String,QueryVar> varNames,Set<OWLNamedIndividual> statics ){
		char varName = 'a';
		CacheQuery q = new CacheQuery();
		
		for(OWLAxiom ax: axioms){
			if(ax instanceof OWLClassAssertionAxiom){
				OWLClassAssertionAxiom clasAss = (OWLClassAssertionAxiom)ax;
				OWLIndividual ind = clasAss.getIndividual();
				QueryInstance indQ = null;
				if(statics.contains(ind)){
					indQ = new QueryVal(ind.toString());
				}
				else{
					if (!varNames.containsKey(ind.toStringID())) {
						indQ = new QueryVar("" + varName++);
						varNames.put(ind.toStringID(), (QueryVar)indQ);
					}else{
						indQ = varNames.get(ind.toStringID());
					}
				}
				String clasIRI = clasAss.getClassExpression().toString();
				q.add(new QueryType(clasIRI, indQ));
			}
			if(ax instanceof OWLObjectPropertyAssertionAxiom){
				OWLObjectPropertyAssertionAxiom objAx = (OWLObjectPropertyAssertionAxiom)ax;
				OWLIndividual subj = objAx.getSubject();
				OWLIndividual obj = objAx.getObject();
				String propIRI = objAx.getProperty().toString();
				QueryInstance subjQ = null;
				QueryInstance objQ = null;
				if(statics.contains(subj)){
					subjQ = new QueryVal(subj.toString());
				}else{
					if(!varNames.containsKey(subj.toStringID())) {
						subjQ = new QueryVar("" + varName++);
						varNames.put(subj.toStringID(), (QueryVar) subjQ);
					}else {
						subjQ = varNames.get(subj.toStringID());
					}
				}
				if(statics.contains(obj)){
					objQ = new QueryVal(obj.toString());
				}else{
					if (!varNames.containsKey(obj.toStringID())) {
						objQ = new QueryVar("" + varName++);
						varNames.put(obj.toStringID(), (QueryVar) objQ);
					}else{
						objQ =varNames.get(obj.toStringID());
					}
				}

				q.add(new QueryProperty(propIRI, subjQ, objQ));
			}

		}
		q.setExpectedNumIndividuals(varNames.size());
		return q;
	}
	public static CacheQuery generateDataProps(CacheQuery q ,Set<OWLAxiom> axioms,Map<String,QueryVar> varNames,Set<OWLNamedIndividual> statics,Map<String,DataConstraint> dataConstrains ){
		char varName = (char) ('a' + q.getNumUsedIndividual());


		for(OWLAxiom ax: axioms){
			if(ax instanceof OWLDataPropertyAssertionAxiom){
				//check if there have been data property restrictions defined
				OWLDataPropertyAssertionAxiom datAx = ((OWLDataPropertyAssertionAxiom) ax);
				if(dataConstrains.containsKey(datAx.getProperty().asOWLDataProperty().toStringID())){
					String propIRI = datAx.getProperty().asOWLDataProperty().toStringID();
					DataConstraint constraint = dataConstrains.get(propIRI);
					QueryInstance subjQ = null;
					OWLIndividual subj =datAx.getSubject();
					if(subj.toStringID().equals(constraint.getSubj())){
						if(!varNames.containsKey(subj.toStringID())) {
							subjQ = new QueryVar("" + varName++);
							varNames.put(subj.toStringID(), (QueryVar) subjQ);
						}else {
							subjQ = varNames.get(subj.toStringID());
						}
					}
					;
					q.addDataConstraint(new QueryDataConstraint(propIRI, subjQ,constraint.getConstraintValue(),constraint.getConstraint()));
				}
			}
		}
		q.setExpectedNumIndividuals(varNames.size());
		return q;
	}
	
	private static Map<String,String> generateVariables(Set<OWLAxiom> axioms){
		return null;
	}
	

}

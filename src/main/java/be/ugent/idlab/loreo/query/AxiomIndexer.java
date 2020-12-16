package be.ugent.idlab.loreo.query;

import be.ugent.idlab.loreo.query.objects.*;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

import java.util.*;

public class AxiomIndexer {

	private Map<String, List<String>> types;
	private Map<String, Map<String, List<String>>> props;
	private Map<String, Map<String, List<String>>> dataProps;
	private Set<String> individuals;

	private Set<String> boundIndividuals;

	public AxiomIndexer() {
		this.types = new HashMap<String, List<String>>();
		this.props = new HashMap<String, Map<String, List<String>>>();
		this.dataProps = new HashMap<String, Map<String, List<String>>>();
		this.individuals = new HashSet<String>();
		this.boundIndividuals = new HashSet<String>();
	}

	public void addAll(Set<OWLAxiom> axioms) throws Exception {
		for (OWLAxiom ax : axioms) {
			add(ax);
		}
	}

	public void add(OWLAxiom ax) throws Exception {
		if (ax instanceof OWLClassAssertionAxiom) {
			add((OWLClassAssertionAxiom) ax);
		} else if (ax instanceof OWLDataPropertyAssertionAxiom) {
			add((OWLDataPropertyAssertionAxiom) ax);
		} else if (ax instanceof OWLObjectPropertyAssertionAxiom) {
			add((OWLObjectPropertyAssertionAxiom) ax);
		} else {
			//System.out.println("unsupported axiom: " + ax.toString());
		}
	}

	public void add(OWLClassAssertionAxiom clsAx) {
		String ind = clsAx.getIndividual().toString();
		String cls = clsAx.getClassExpression().toString();
		if (!types.containsKey(cls)) {
			types.put(cls, new ArrayList<String>());
		}
		types.get(cls).add(ind);
		individuals.add(ind);
	}

	public void add(OWLObjectPropertyAssertionAxiom ax) {
		String prop = ax.getProperty().toString();
		String subj = ax.getSubject().toString();
		String obj = ax.getObject().toString();
		if (!props.containsKey(prop)) {
			props.put(prop, new HashMap<String, List<String>>());
		}
		if (!props.get(prop).containsKey(subj)) {
			props.get(prop).put(subj, new ArrayList<String>());
		}
		props.get(prop).get(subj).add(obj);
		individuals.add(subj);
		individuals.add(obj);
	}

	public void add(OWLDataPropertyAssertionAxiom ax) {
		String prop = ax.getProperty().asOWLDataProperty().toStringID();
		String subj = ax.getSubject().toString();
		String value = ax.getObject().getLiteral();
		if (!dataProps.containsKey(prop)) {
			dataProps.put(prop, new HashMap<String, List<String>>());
		}
		if (!dataProps.get(prop).containsKey(subj)) {
			dataProps.get(prop).put(subj, new ArrayList<String>());
		}
		dataProps.get(prop).get(subj).add(value);
		individuals.add(subj);
	}
	public int getNumberOfIndividuals(){
		return individuals.size();
	}
	// public boolean checkQuery_old(CacheQuery query) {
	// for (QueryObject qObj : query.getQuery()) {
	// if (qObj instanceof QueryType) {
	// QueryType qType = (QueryType) qObj;
	// String type = qType.getType();
	// QueryVar var = (QueryVar) qType.getInst();
	// var.addIntersect(types.get(type));
	// } else if (qObj instanceof QueryProperty) {
	// QueryProperty qProp = (QueryProperty) qObj;
	// String prop = qProp.getProp();
	// QueryVar var = (QueryVar) qProp.getSubj();
	// QueryVar var2 = (QueryVar) qProp.getObj();
	// List<String> removes = new ArrayList<String>();
	// for (String subjString : var.getValues()) {
	// if (props.get(prop).containsKey(subjString)) {
	// var2.addUnion(props.get(prop).get(subjString));
	// qProp.addMapping(subjString, props.get(prop).get(subjString));
	// } else {
	// removes.add(subjString);
	// }
	//
	// }
	// var.remove(removes);
	// }
	// System.out.println(query);
	// }
	// return false;
	// }

	public boolean checkQuery(CacheQuery query) {
		query.reset();
		boundIndividuals.addAll(query.getUsedIndividuals());
		return checkQuery_help(query, 0);
	}

	public boolean checkQuery_help(CacheQuery query, int index) {
		if (index < query.getQuery().size()) {
			QueryObject qObj = query.getQuery().get(index);
			if (qObj instanceof QueryType) {
				QueryType qType = (QueryType) qObj;
				String type = qType.getType();
				QueryInstance var = qType.getInst();
				if (var.isSet()) { // already set, check if comform with
									// the type
					if (types.containsKey(type) && types.get(type).contains(var.getValue())) {
						// type is correct
						if(checkQuery_help(query, index + 1)){
							return true;
						}
					} else {
						return false;
					}
				} else { // first time we see the variable
					if(types.containsKey(type)) {
						for (String indType : types.get(type)) {
							if (!checkIfIndividualAlreadyBound(indType)) {
								bindVariable(var, indType);
								if (checkQuery_help(query, index + 1)) {
									return true;
								}
								unbindVariable(var, indType);

							}
						}
					}
				}
			} else if (qObj instanceof QueryProperty) {
				QueryProperty qProp = (QueryProperty) qObj;
				String prop = qProp.getProp();
				QueryInstance var = qProp.getSubj();
				QueryInstance var2 = qProp.getObj();
				if (var.isSet()) {
					String subjInd = var.getValue();

					if(checkProp(query, index, prop, subjInd, var2)){
						return true;
					}
				} else if(props.containsKey(prop)){
					for (String subjInd : props.get(prop).keySet()) {
						//we first check if no other variable has already been bound to the specific individual
						//remember that the shapes have to match exactly
						if(!checkIfIndividualAlreadyBound(subjInd)) {
							bindVariable(var,subjInd);
							if (checkProp(query, index, prop, subjInd, var2)) {
								return true;
							}
							unbindVariable(var,subjInd);
						}

					}
				}
			}

		} else {
			//time to check the data constrains if any
			for(QueryDataConstraint dataQ: query.getDataConstraints()){
				if(dataQ.getSubj().isSet()){
					String subj = dataQ.getSubj().getValue();
					if(dataProps.get(dataQ.getDataProp()).containsKey(subj)){
						for(String literal: dataProps.get(dataQ.getDataProp()).get(subj)){
							if(!evaluateConstraint(literal,dataQ.getConstraintValue(),dataQ.getConstraint())){
								return false;
							}

						}
					}
				}else{
					return false;
				}
			}
			return true;

		}
		return false;
	}
	private boolean evaluateConstraint(String literal, double value, QueryDataConstraint.Constraint c){
		Double parseLit = Double.parseDouble(literal);
		if(c.equals(QueryDataConstraint.Constraint.GreaterThan)){
			return parseLit > value;
		}else if(c.equals(QueryDataConstraint.Constraint.GreaterThanOrEqual)){
			return parseLit >= value;
		}else if(c.equals(QueryDataConstraint.Constraint.LessThan)){
			return parseLit < value;
		}else if(c.equals(QueryDataConstraint.Constraint.LessThanOrEqual)){
			return parseLit <= value;
		}else{
			return false;
		}
	}
	/***
	 * Binds individual to variable and marks individual as already binded such that it will not be bound to a new variable.
	 * @param var the query variable to bind the individual to
	 * @param individual the individual that should be bind to the variable
	 */
	private void bindVariable(QueryInstance var,String individual){
		var.add(individual);
		boundIndividuals.add(individual);
	}

	/***
	 * Unbinds an variable
	 * @param var the variable to be unbind
	 * @param individual the individual to be unbind.
	 */
	private void unbindVariable(QueryInstance var,String individual){
		var.remove();
		boundIndividuals.remove(individual);
	}

	/***
	 * Checks the query to evaluate if an individual has already been bound to a certain variable.
	 * @param ind the individual to check for variable bindings
	 * @return true if individual has been bound to variable, false otherwise.
	 */
	private boolean checkIfIndividualAlreadyBound(String ind){
		return boundIndividuals.contains(ind);
	}
	private boolean checkProp(CacheQuery query,int index, String prop, String subjInd,QueryInstance var2){
		if (props.containsKey(prop)&&props.get(prop).containsKey(subjInd)) {
			if (!var2.isSet()) {
				for (String indObj : props.get(prop).get(subjInd)) {
					if(!checkIfIndividualAlreadyBound(indObj)) {
						bindVariable(var2,indObj);
						if (checkQuery_help(query, index + 1)) {
							return true;
						}
						unbindVariable(var2,indObj);
					}
				}
			} else {
				if (props.get(prop).get(subjInd).contains(var2.getValue())) {
					if(checkQuery_help(query, index + 1)){
						return true;
					}
				} else {
					return false;
				}
			}
		}
		return false;
	}
}

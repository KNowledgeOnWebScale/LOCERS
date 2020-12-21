package be.ugent.idlab.locers.query;

import be.ugent.idlab.locers.query.objects.*;

import java.util.*;

public class CacheQuery {

	private List<QueryObject> queryObjects;
	private List<QueryDataConstraint> dataConstraints;
	private int expectedIndividuals;
	
	public CacheQuery(){
		queryObjects = new ArrayList<QueryObject>();
		dataConstraints = new ArrayList<QueryDataConstraint>();
	}
	public CacheQuery add(QueryObject qObj){
		queryObjects.add(qObj);
		return this;
	}
	public CacheQuery addDataConstraint(QueryDataConstraint qObj){
		dataConstraints.add(qObj);
		return this;
	}
	
	public List<QueryObject> getQuery(){
		return queryObjects;
	}

	public List<QueryDataConstraint> getDataConstraints(){ return dataConstraints;}
	
	public String toString(){
		StringBuilder sb = new StringBuilder("Type(");
		for(QueryObject qo: queryObjects){
			sb.append(qo);
			sb.append(", ");
		}
		return sb.toString();
	}
	public Set<String> getUsedIndividuals(){
		Set<String> vals = new HashSet<String>();
		for(QueryObject qObj: queryObjects){
			vals.addAll(qObj.getIndividuals());
		}
		return vals;
	}
	public int getNumUsedIndividual(){
		Set<String> vals = this.getUsedIndividuals();
		return vals.size();
	}
	public void setExpectedNumIndividuals(int inds){
		this.expectedIndividuals=inds;
	}
	public boolean isFullMatch(){
		return expectedIndividuals == getNumUsedIndividual();
	}
	public void reset(){
		for(QueryObject qObj: queryObjects){
			qObj.reset();
		}
	}
	
	public int generateHashCode(){
		int result = 17;
		int c =0;
		for(QueryObject qObj: queryObjects){
			c+=qObj.generateHashCode();
		}
		return 31*result + c;
	}
	public Map<String,String> getBindings(){
		Map<String,String> bindings = new HashMap<String,String>();
		for(QueryObject qObj: queryObjects){
			if(qObj instanceof QueryType){
				QueryInstance qInst = ((QueryType)qObj).getInst();
				bindings.put(qInst.getName(),qInst.getValue());
			}
			if(qObj instanceof QueryProperty){
				QueryInstance qSubj = ((QueryProperty)qObj).getSubj();
				QueryInstance qObject = ((QueryProperty)qObj).getObj();
				bindings.put(qSubj.getName(),qSubj.getValue());
				bindings.put(qObject.getName(),qObject.getValue());
			}
		}
		return bindings;
	}
	public void addDataPropConstraint(String var, String dataProp, double constraintValue, QueryDataConstraint.Constraint c){
		for(QueryObject qObj: queryObjects){

		}
		//dataConstraints.add(new QueryDataConstraint(dataProp,))
	}
}

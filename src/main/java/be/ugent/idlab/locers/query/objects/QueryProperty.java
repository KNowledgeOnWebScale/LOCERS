package be.ugent.idlab.locers.query.objects;

import java.util.*;

public class QueryProperty implements QueryItem{

	private String prop;
	private QueryInstance subj;
	private QueryInstance obj;

	private Map<String,Collection<String>> propVarMapping;
	public QueryProperty(String prop, QueryInstance subj, QueryInstance obj){
		this.prop = prop;
		this.subj = subj;
		this.obj = obj;
		propVarMapping = new HashMap<String,Collection<String>>();
	}

	public String getProp() {
		return prop;
	}

	public QueryInstance getSubj() {
		return subj;
	}

	public QueryInstance getObj() {
		return obj;
	}
	public void addMapping(String subj,Collection<String> obj){
		if(!propVarMapping.containsKey(subj)){
			propVarMapping.put(subj, new HashSet<String>());
		}
		propVarMapping.get(subj).addAll(obj);
	}
	public String toString(){
		StringBuilder sb = new StringBuilder("Prop(");
		sb.append(prop);
		sb.append(")  Subj:  ");
		sb.append(subj);
		sb.append(" Obj: ");
		sb.append(obj);

		
		return sb.toString();
	}

	@Override
	public Set<String> getIndividuals() {
		Set<String> inds = new HashSet(2);
		inds.addAll(subj.getIndividuals());
		inds.addAll(obj.getIndividuals());
		return inds;
	}
	@Override
	public void reset(){
		subj.reset();
		obj.reset();
	}
	@Override
	public int generateHashCode(){
		int result = 17;
		int c =0;
		c+=prop.hashCode();
		return 31*result + c;
	}
	
}

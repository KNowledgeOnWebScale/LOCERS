package be.ugent.idlab.locers.query.objects;

import java.util.Set;

public class QueryVar extends QueryInstance {

	private Set<String> possibleValues;
	private String possibleValue;

	public QueryVar(String name) {
		super();
		this.name = name;
		this.value = null;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		if(value!=null){
			sb.append("Var(").append(name)
			.append(") value: ").append(value);
		}else{
			sb.append("Var(").append(name)
			.append(") ");
			
		}
		return sb.toString();
	}
	
}

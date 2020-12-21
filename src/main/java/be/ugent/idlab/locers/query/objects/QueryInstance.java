package be.ugent.idlab.locers.query.objects;

import java.util.Collections;
import java.util.Set;

public class QueryInstance implements QueryObject{

	protected String value;
	protected String name;
	public QueryInstance(){
		this.value = null;
		this.name=null;
	}
	public String getValue() {
		return value;
	}
	public String getName(){
		return name;
	}
	public boolean isSet(){
		return value!=null;
	}
	public void remove(){
		value = null;
	}
	public void add(String newValue){
		this.value= newValue;
	}
	@Override
	public Set<String> getIndividuals() {
		if(value!=null){
			return Collections.singleton(value);
		}else{
			return Collections.emptySet();
		}
	}
	
	public void reset(){
		value = null;
	}
	@Override
	public int generateHashCode(){
		
		return 0;
	}
}

package be.ugent.idlab.loreo.query.objects;

public class QueryVal extends QueryInstance{

	public QueryVal(String value) {
		super();
		this.value=value;
		this.name = value.substring(1,value.length()-1);
	}
	public void remove(){
		
	}
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Val(").append(value)
		.append(") ");
		return sb.toString();
	}
	public void reset(){
		//does nothing

	}
}

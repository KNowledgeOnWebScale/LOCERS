package be.ugent.idlab.locers.query.objects;

import java.util.Set;

public class QueryType implements QueryItem{

	private String type;
	private QueryInstance inst;

	public QueryType(String type, QueryInstance inst){
		this.type = type;
		this.inst = inst;
	}

	public String getType() {
		return type;
	}

	public QueryInstance getInst() {
		return inst;
	}
	public String toString(){
		StringBuilder sb = new StringBuilder("Type(");
		sb.append(type);
		sb.append(") Values: ");
		sb.append(inst);
		return sb.toString();
	}

	@Override
	public Set<String> getIndividuals() {
		// TODO Auto-generated method stub
		return inst.getIndividuals();
	}
	@Override
	public void reset(){
		inst.reset();
	}

	/* (non-Javadoc)
	 * @see be.ugent.idlab.loreo.query.objects.QueryObject#generateHashCode()
	 */
	@Override
	public int generateHashCode(){
		int result = 17;
		int c =0;
		c+=type.hashCode();
		return 31*result + c;
	}
}

package be.ugent.idlab.loreo.query.objects;

import java.util.Set;

public interface QueryObject {

	public  Set<String> getIndividuals();
	
	public void reset();
	
	public int generateHashCode();
}

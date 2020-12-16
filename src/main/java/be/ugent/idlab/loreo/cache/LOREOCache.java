package be.ugent.idlab.loreo.cache;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

public interface LOREOCache {
	
	public void init(OWLOntology ontology);
	
	public boolean addCQ(OWLClass target,OWLClassExpression cq);
	
	public Set<OWLClass> check(Set<OWLAxiom> event);

}

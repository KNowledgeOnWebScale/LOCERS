package be.ugent.idlab.locers.cache;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

public interface LOCERSCache {
	
	public void init(OWLOntology ontology);
	
	public boolean addCQ(OWLClass target,OWLClassExpression cq);
	
	public Set<OWLClass> check(Set<OWLAxiom> event);

}

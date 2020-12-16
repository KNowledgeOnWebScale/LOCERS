package be.ugent.idlab.loreo.cache;

import be.ugent.idlab.loreo.query.CacheQuery;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.Map;
import java.util.Set;

public interface LOREOCacheStructure {
    public Set<OWLClass> check(Set<OWLAxiom> ontology);
    public void add(CacheQuery q, Set<OWLClass> targets);
    public void addMaterialize(CacheQuery q, Map<String,Set<OWLClass>> targets);
    public int getSize();
}

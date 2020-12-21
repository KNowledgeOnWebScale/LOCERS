/**
 * 
 */
package be.ugent.idlab.locers;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import com.clarkparsia.owlapi.explanation.SatisfiabilityConverter;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.Collections;
import java.util.Set;

/**
 * @author pbonte
 *
 */
public class Explainer {

	private OWLOntology ontology;
	private OWLReasoner reasoner;
	private OWLOntologyManager manager;
	private HSTExplanationGenerator multExplanator;

	public Explainer(OWLOntology ontology, OWLReasoner reasoner) {
		this.ontology = ontology;
		this.manager = ontology.getOWLOntologyManager();
		this.reasoner = reasoner;
		ReasonerFactory factory = new ReasonerFactory();

		BlackBoxExplanation exp = new BlackBoxExplanation(ontology, factory, reasoner);
		this.multExplanator = new HSTExplanationGenerator(exp);
	}

	public Set<OWLAxiom> explain(OWLIndividual ind, OWLClass c){
		OWLAxiom ax = manager.getOWLDataFactory().getOWLClassAssertionAxiom(c,ind );
		SatisfiabilityConverter			converter = new SatisfiabilityConverter( manager.getOWLDataFactory() );
		int maxSize = 0;
		Set<OWLAxiom> masSet = Collections.emptySet();
		for(Set<OWLAxiom> explanations:multExplanator.getExplanations(converter.convert(ax))){
			if(explanations.size()>maxSize){
				maxSize = explanations.size();
				masSet = explanations;
			}
		}
		return masSet;
	}
}

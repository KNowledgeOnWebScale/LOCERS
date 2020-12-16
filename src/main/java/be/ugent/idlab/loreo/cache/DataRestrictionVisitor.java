package be.ugent.idlab.loreo.cache;

import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataRestrictionVisitor implements OWLClassExpressionVisitor,OWLDataVisitor {
    private List<String> objectProps ;
    private String dataProp ;
    private String value;
    private String restriction;
    public DataRestrictionVisitor(){
        this.objectProps = new ArrayList<String>();
    }
    public void visit(OWLObjectIntersectionOf ce){

        ce.conjunctSet().forEach(v -> v.accept(this));
    }
    public void visit(OWLObjectUnionOf ce){
        ce.conjunctSet().forEach(v -> v.accept(this));
    }
    public void visit(OWLObjectComplementOf ce){
        ce.conjunctSet().forEach(v -> v.accept(this));
    }
    public void visit(OWLObjectSomeValuesFrom ce){
        objectProps.add(ce.getProperty().getNamedProperty().toStringID());
        ce.classesInSignature().forEach(v -> v.accept(this));
    }
    public void visit(OWLObjectAllValuesFrom ce){
        objectProps.add(ce.getProperty().getNamedProperty().toStringID());
        ce.classesInSignature().forEach(v -> v.accept(this));
    }
    public void  visit(OWLDataSomeValuesFrom ce){
        OWLDataProperty prop = ce.dataPropertiesInSignature().findAny().get();
        dataProp = prop.toStringID();
       ce.getFiller().accept(this);
        OWLDatatypeRestriction t = (OWLDatatypeRestriction)ce.getFiller();
        for(OWLFacetRestriction restriction:t.facetRestrictionsAsList()){
           this.value = restriction.getFacetValue().getLiteral();
            this.restriction = restriction.getFacet().toString();

        }


    }

    public List<String> getObjectProps() {
        return objectProps;
    }

    public String getDataProp() {
        return dataProp;
    }

    public String getValue() {
        return value;
    }

    public String getRestriction() {
        return restriction;
    }
}

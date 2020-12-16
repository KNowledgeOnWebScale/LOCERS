package be.ugent.idlab.loreo.query.objects;

import java.util.Set;

public class QueryDataConstraint implements QueryObject{


    public enum Constraint {
        GreaterThan,
        GreaterThanOrEqual,
        LessThan,
        LessThanOrEqual
    }
    private Constraint c;
    private String dataProp;
    private QueryInstance subj;
    private double constraintValue;
    public QueryDataConstraint(String dataProp,QueryInstance subj,double constraintValue,Constraint c){
        this.dataProp = dataProp;
        this.subj = subj;
        this.constraintValue = constraintValue;
        this.c = c;
    }

    public Constraint getConstraint() {
        return c;
    }

    public String getDataProp() {
        return dataProp;
    }

    public QueryInstance getSubj() {
        return subj;
    }

    public double getConstraintValue() {
        return constraintValue;
    }
    @Override
    public Set<String> getIndividuals() {
        return null;
    }

    @Override
    public void reset() {

    }

    @Override
    public int generateHashCode() {
        return 0;
    }
}

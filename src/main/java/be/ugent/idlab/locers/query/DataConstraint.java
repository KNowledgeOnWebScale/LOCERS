package be.ugent.idlab.locers.query;

import be.ugent.idlab.locers.query.objects.QueryDataConstraint;

public class DataConstraint  {



    private QueryDataConstraint.Constraint c;
    private String dataProp;
    private String subj;
    private double constraintValue;
    public DataConstraint(String dataProp, String subj, double constraintValue, QueryDataConstraint.Constraint c){
        this.dataProp = dataProp;
        this.subj = subj;
        this.constraintValue = constraintValue;
        this.c = c;
    }
    public DataConstraint(String dataProp, String value, String constraint){
        this.dataProp = dataProp;
        this.constraintValue = Double.parseDouble(value);
        this.c = this.convertConstraint(constraint);
    }
    public QueryDataConstraint.Constraint convertConstraint(String constraint){
        switch(constraint){
            case "minInclusive":
                return QueryDataConstraint.Constraint.GreaterThanOrEqual;
            case "minExclusive":
                return QueryDataConstraint.Constraint.GreaterThan;
            case "maxExclusive":
                return QueryDataConstraint.Constraint.LessThan;
           default:
                return QueryDataConstraint.Constraint.LessThanOrEqual;


        }
    }
    public QueryDataConstraint.Constraint getConstraint() {
        return c;
    }

    public String getDataProp() {
        return dataProp;
    }

    public String getSubj() {
        return subj;
    }

    public double getConstraintValue() {
        return constraintValue;
    }

}

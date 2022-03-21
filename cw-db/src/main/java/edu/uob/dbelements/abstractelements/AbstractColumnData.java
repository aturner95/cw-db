package edu.uob.dbelements.abstractelements;

abstract public class AbstractColumnData {
    protected String data;

    /**
     * This function is abstract for either the ColumnHeading's name or Record's attribute value
     * @return
     */
    public String getData(){
        return this.data;
    }

    /**
     * This function is abstract for either the ColumnHeading's name or Record's attribute value
     * @param data
     */
    public void setData(String data){
        this.data = data;
    }
}

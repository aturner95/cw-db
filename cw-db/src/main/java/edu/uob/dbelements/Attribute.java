package edu.uob.dbelements;

import edu.uob.abstractelements.AbstractColumnData;

public class Attribute extends AbstractColumnData {

    public Attribute(String value){
        this.data = value;
    }

    public String getValue() {
        return data;
    }

    public void setValue(String value) {
        this.data = value;
    }

}

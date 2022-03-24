package edu.uob.dbelements;

import edu.uob.dbelements.abstractelements.AbstractColumnData;

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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        final Attribute other = (Attribute) obj;
        return this.getValue() != null && other.getValue() != null && this.getValue().equalsIgnoreCase(other.getValue());
    }


    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.data != null ? this.data.hashCode() : 0);
        return hash;
    }

}

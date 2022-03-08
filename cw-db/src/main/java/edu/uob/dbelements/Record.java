package edu.uob.dbelements;

import java.util.ArrayList;
import java.util.List;

public class Record {

    private List<Attribute> attributes;

    public Record(){

    }

    public Record(List<Attribute> attributes){
        this.attributes = attributes;
    }

    public long getId() {
        return Long.valueOf(attributes.get(0).getValue());
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }
}

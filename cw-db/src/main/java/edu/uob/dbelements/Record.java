package edu.uob.dbelements;

import java.util.List;

public class Record {

    private long Id;
    private List<Attribute> attributes;

    public Record(){

    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }
}

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

    public void addDefaultAttribute(){
        // TODO adding an empty String causes an issue; because the String is empty, nothing is
        // written to the DBFile. Therefore, nothing is read back! For now, populate with a space
        Attribute attribute = new Attribute(" ");
        attributes.add(attribute);
    }
}

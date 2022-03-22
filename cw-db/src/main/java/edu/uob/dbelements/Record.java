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

    public String getId() {
        return attributes.get(0).getValue();
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final Record other = (Record) obj;
        if(this.getId() != null && other.getId() != null && this.getId().equals(other.getId())){
            return true;
        }

        return false;
    }
}

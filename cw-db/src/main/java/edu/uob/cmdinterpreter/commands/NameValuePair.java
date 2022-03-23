package edu.uob.cmdinterpreter.commands;

import edu.uob.dbelements.ColumnHeader;

public class NameValuePair {

    private String name;
    private String value;

    public NameValuePair(String name, String value){
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        final NameValuePair other = (NameValuePair) obj;
        if(this.name != null && other.getName() != null && this.name.equals(other.getName())){
            return true;
        }

        return false;
    }


    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.getValue().hashCode() : 0);
        hash = 53 * hash + this.name.hashCode();
        return hash;
    }
}

package edu.uob.dbelements;

import edu.uob.dbelements.abstractelements.AbstractColumnData;

public class ColumnHeader extends AbstractColumnData {

    public ColumnHeader(String colName){
        this.data = colName;
    }

    public String getColName() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        final ColumnHeader other = (ColumnHeader) obj;
        return this.data != null && other.getData() != null && this.data.equalsIgnoreCase(other.getData());
    }


    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.data != null ? this.getColName().hashCode() : 0);
        hash = 53 * hash + this.toString().hashCode();
        return hash;
    }

}

package edu.uob.dbelements;

import edu.uob.dbfilesystem.EntityRelationshipType;

public class EntityRelationship {

    private EntityRelationshipType relationshipType;

    public EntityRelationship(){

    }

    public EntityRelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(EntityRelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }
}

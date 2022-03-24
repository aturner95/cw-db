package edu.uob.exceptions;

import java.io.Serial;

public class QueryException extends Exception{

    @Serial
    private static final long serialVersionUID = -7342988237478234L;

    public QueryException(String message) {
        super(message);
    }

    public static class AttributeNotFoundException extends QueryException {

        @Serial
        private static final long serialVersionUID = 47398294623844324L;

        public AttributeNotFoundException(String attributeName) {
            super("Attribute does not exist on table: " + attributeName);
        }
    }

}

package edu.uob.exceptions;

public class QueryException extends Exception{
    private static final long serialVersionUID = -7342988237478234L;

    private static final String ERR_UNKNOWN = "An unknown QueryException has occurred";

    public QueryException() {
        super(ERR_UNKNOWN);
    }

    public QueryException(String message) {
        super(message);
    }

    public static class AttributeNotFoundException extends QueryException {
        private static final long serialVersionUID = 47398294623844324L;

        public AttributeNotFoundException(String attributeName) {
            super("Attribute does not exist on table: " + attributeName);
        }
    }

}

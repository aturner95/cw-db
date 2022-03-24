package edu.uob.exceptions;

public class DBException extends Exception {

    private static final long serialVersionUID = -9384902384092843L;

    public static final String ERR_UNKNOWN = "An unknown DBException has occurred";

    public DBException() {
        super(ERR_UNKNOWN);
    }

    public DBException(String message) {
        super(message);
    }

    public static class DBExistsException extends DBException {
        private static final long serialVersionUID = -237489438095435L;

        public DBExistsException(String dbName) {
            super("Table already exists: " + dbName);
        }
    }

    public static class DBDoesNotExistException extends DBException {
        private static final long serialVersionUID = 783202793903458L;

        public DBDoesNotExistException(String dbName) {
            super("Database does not exist: " + dbName);
        }
    }

    public static class DBTableExistsException extends DBException {
        private static final long serialVersionUID = 9238478972374983L;

        public DBTableExistsException(String tableName) {
            super("Table already exists: " + tableName);
        }
    }

    public static class DBTableDoesNotExistException extends DBException {
        private static final long serialVersionUID = 8061335505061402995L;

        public DBTableDoesNotExistException(String tableName) {
            super("Table does not exist: " + tableName);
        }
    }

    public static class DBAttributeExistsException extends DBException {

        private static final long serialVersionUID = 3292975112861815343L;

        public DBAttributeExistsException(String attributeName) {
            super("Attribute already exists: " + attributeName);
        }
    }

    public static class DBAttributeDoesNotExistException extends DBException {

        private static final long serialVersionUID = 3242342390432048230L;

        public DBAttributeDoesNotExistException(String attributeName) {
            super("Attribute does not exist: " + attributeName);
        }
    }

    public static class DBInvalidAttributeListException extends DBException {

        private static final long serialVersionUID = 124354098763454L;

        public DBInvalidAttributeListException(int expectedSize, int actualSize) {
            super("Expected attribute list:" + expectedSize + ", but got: " + actualSize);
        }
    }

    public static class DBEntityExistsException extends DBException {

        private static final long serialVersionUID = -2346384723865437534L;

        public DBEntityExistsException(String key) {
            super("Entity already exists with key: " + key);
        }
    }

}
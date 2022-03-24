package edu.uob.exceptions;

import java.io.Serial;

public class DBException extends Exception {

    @Serial
    private static final long serialVersionUID = -9384902384092843L;

    public static final String ERR_UNKNOWN = "An unknown DBException has occurred";

    public DBException() {
        super(ERR_UNKNOWN);
    }

    public DBException(String message) {
        super(message);
    }

    public static class DBExistsException extends DBException {
        @Serial
        private static final long serialVersionUID = -237489438095435L;

        public DBExistsException(String dbName) {
            super("Database already exists: " + dbName);
        }
    }

    public static class DBDoesNotExistException extends DBException {
        @Serial
        private static final long serialVersionUID = 783202793903458L;

        public DBDoesNotExistException(String dbName) {
            super("Database does not exist: " + dbName);
        }
    }

    public static class DBTableExistsException extends DBException {
        @Serial
        private static final long serialVersionUID = 9238478972374983L;

        public DBTableExistsException(String tableName) {
            super("Table already exists: " + tableName);
        }
    }

    public static class DBTableDoesNotExistException extends DBException {
        @Serial
        private static final long serialVersionUID = 8061335505061402995L;

        public DBTableDoesNotExistException(String tableName) {
            super("Table does not exist: " + tableName);
        }
    }

    public static class DBAttributeExistsException extends DBException {

        @Serial
        private static final long serialVersionUID = 3292975112861815343L;

        public DBAttributeExistsException(String attributeName) {
            super("Attribute already exists: " + attributeName);
        }
    }

    public static class DBAttributeDoesNotExistException extends DBException {

        @Serial
        private static final long serialVersionUID = 3242342390432048230L;

        public DBAttributeDoesNotExistException(String attributeName) {
            super("Attribute does not exist: " + attributeName);
        }
    }

    public static class DBInvalidAttributeListException extends DBException {

        @Serial
        private static final long serialVersionUID = 124354098763454L;

        public DBInvalidAttributeListException(int expectedSize, int actualSize) {
            super("Expected attribute list:" + expectedSize + ", but got: " + actualSize);
        }
    }

}

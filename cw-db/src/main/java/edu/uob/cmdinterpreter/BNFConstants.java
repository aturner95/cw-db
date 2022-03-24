package edu.uob.cmdinterpreter;

public class BNFConstants {

    // Query language key words
    public static final String FROM = "FROM";
    public static final String WHERE = "WHERE";
    public static final String DATABASE = "DATABASE";
    public static final String TABLE = "TABLE";
    public static final String INTO = "INTO";
    public static final String VALUES = "VALUES";
    public static final String NULL = "NULL";
    public static final String SET = "SET";
    public static final String AND = "AND";
    public static final String ON = "ON";
    public static final String OR ="OR";

    // Command types
    public static final String USE = "USE";
    public static final String CREATE = "CREATE";
    public static final String DROP = "DROP";
    public static final String ALTER = "ALTER";
    public static final String INSERT = "INSERT";
    public static final String SELECT = "SELECT";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String JOIN = "JOIN";

    // Symbol characters
    public static final String EXCLAMATION_MARK = "!";
    public static final String HASH_SYMBOL = "#";
    public static final String DOLLAR_SIGN= "$";
    public static final String PERCENT_SYMBOL= "%";
    public static final String AMPERSAND = "&";
    public static final String LEFT_BRACKET = "(";
    public static final String RIGHT_BRACKET = ")";
    public static final String STAR_SYMBOL = "*";
    public static final String PLUS_SYMBOL= "+";
    public static final String COMMA = ",";
    public static final String DASH_SYMBOL = "-";
    public static final String PERIOD = ".";
    public static final String FORWARD_SLASH = "/";
    public static final String COLON = ":";
    public static final String SEMI_COLON= ";";
    public static final String RIGHT_ANGLE= ">";
    public static final String EQUALS_SYMBOL = "=";
    public static final String LEFT_ANGLE = "<";
    public static final String QUESTION_MARK = "?";
    public static final String AT_SYMBOL = "@";
    public static final String LEFT_SQR_BRACKET = "{";
    public static final String BACK_SLASH = "\\";
    public static final String RIGHT_SQR_BRACKET = "]";
    public static final String HAT_SYMBOL = "^";
    public static final String UNDERSCORE = "?_";
    public static final String TAG_SYMBOL = "`";
    public static final String LEFT_BRACE = "{";
    public static final String RIGHT_BRACE = "}";
    public static final String TILDA = "~";
    public static final String SINGLE_QUOTATION = "'";

    // Alteration type (DROP included under commands)
    public static final String ADD = "ADD";

    // Boolean literals
    public static final String TRUE = "TRUE";
    public static final String FALSE = "FALSE";


    // Operator characters
    public static final String EQUAL_TO = "==";
    public static final String GREATER_THAN = ">";
    public static final String LESS_THAN = "<";
    public static final String GREATER_OR_EQUAL_TO = ">=";
    public static final String LESS_OR_EQUAL_TO = "<=";
    public static final String NOT_EQUAL_TO = "!=";
    public static final String LIKE ="LIKE";

    private BNFConstants(){

    }
}

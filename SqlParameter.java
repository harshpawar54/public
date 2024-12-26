public class SqlParameter {
    private final int type;         // SQL type (e.g., Types.INTEGER, Types.VARCHAR)
    private final Object value;    // Value of the parameter
    private final boolean isOutParameter; // Indicates if it's an OUT parameter

    public SqlParameter(int type, Object value) {
        this(type, value, false);
    }

    public SqlParameter(int type, Object value, boolean isOutParameter) {
        this.type = type;
        this.value = value;
        this.isOutParameter = isOutParameter;
    }

    public int getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public boolean isOutParameter() {
        return isOutParameter;
    }
}
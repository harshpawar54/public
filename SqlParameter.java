public class SqlParameter {
    private final int type;  // SQL type (e.g., Types.INTEGER, Types.VARCHAR)
    private final Object value;  // Value of the parameter

    public SqlParameter(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}
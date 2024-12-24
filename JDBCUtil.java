import java.sql.*;
import java.util.Map;

public class JDBCUtil {

    /**
     * Executes a parameterized SELECT query and processes the ResultSet.
     *
     * @param conn       The Connection object
     * @param sql        The SQL query with placeholders
     * @param parameters A map of parameter index to SqlParameter objects
     * @param handler    A ResultSetHandler to process the ResultSet
     * @param <T>        The type of the processed result
     * @return The processed result
     * @throws SQLException if a database access error occurs
     */
    public static <T> T executeQuery(Connection conn, String sql, Map<Integer, SqlParameter> parameters, ResultSetHandler<T> handler) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, parameters);
            try (ResultSet rs = pstmt.executeQuery()) {
                return handler.handle(rs);
            }
        }
    }

    /**
     * Executes a parameterized DML query (INSERT, UPDATE, DELETE).
     *
     * @param conn       The Connection object
     * @param sql        The SQL query with placeholders
     * @param parameters A map of parameter index to SqlParameter objects
     * @return Number of rows affected
     * @throws SQLException if a database access error occurs
     */
    public static int executeUpdate(Connection conn, String sql, Map<Integer, SqlParameter> parameters) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, parameters);
            return pstmt.executeUpdate();
        }
    }

    /**
     * Sets the parameters for a PreparedStatement based on their SQL types.
     *
     * @param pstmt      The PreparedStatement
     * @param parameters A map of parameter index to SqlParameter objects
     * @throws SQLException if a database access error occurs
     */
    private static void setParameters(PreparedStatement pstmt, Map<Integer, SqlParameter> parameters) throws SQLException {
        for (Map.Entry<Integer, SqlParameter> entry : parameters.entrySet()) {
            int index = entry.getKey();
            SqlParameter param = entry.getValue();

            if (param.getValue() == null) {
                pstmt.setNull(index, param.getType());
            } else {
                switch (param.getType()) {
                    case Types.INTEGER:
                        pstmt.setInt(index, (Integer) param.getValue());
                        break;
                    case Types.BIGINT:
                        pstmt.setLong(index, (Long) param.getValue());
                        break;
                    case Types.FLOAT:
                    case Types.REAL:
                        pstmt.setFloat(index, (Float) param.getValue());
                        break;
                    case Types.DOUBLE:
                        pstmt.setDouble(index, (Double) param.getValue());
                        break;
                    case Types.VARCHAR:
                    case Types.CHAR:
                    case Types.LONGVARCHAR:
                        pstmt.setString(index, (String) param.getValue());
                        break;
                    case Types.BOOLEAN:
                        pstmt.setBoolean(index, (Boolean) param.getValue());
                        break;
                    case Types.DATE:
                        pstmt.setDate(index, (Date) param.getValue());
                        break;
                    case Types.TIMESTAMP:
                        pstmt.setTimestamp(index, (Timestamp) param.getValue());
                        break;
                    case Types.TIME:
                        pstmt.setTime(index, (Time) param.getValue());
                        break;
                    case Types.BLOB:
                        pstmt.setBlob(index, (Blob) param.getValue());
                        break;
                    case Types.CLOB:
                        pstmt.setClob(index, (Clob) param.getValue());
                        break;
                    case Types.ARRAY:
                        pstmt.setArray(index, (Array) param.getValue());
                        break;
                    default:
                        pstmt.setObject(index, param.getValue(), param.getType());
                        break;
                }
            }
        }
    }
}
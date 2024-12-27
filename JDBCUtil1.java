import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JDBCUtil {

    /**
     * Executes an INSERT statement and retrieves generated keys.
     *
     * @param conn         The database connection.
     * @param sql          The INSERT SQL statement.
     * @param parameters   The map of parameters for the prepared statement.
     * @return List of generated keys as a result of the INSERT.
     * @throws SQLException If a database access error occurs.
     */
    public static List<Object> executeInsertWithGeneratedKeys(Connection conn, String sql, Map<Integer, SqlParameter> parameters) throws SQLException {
        List<Object> generatedKeys = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(stmt, parameters);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    while (keys.next()) {
                        generatedKeys.add(keys.getObject(1)); // Retrieve the first column as the key
                    }
                }
            }
        }

        return generatedKeys;
    }

    /**
     * Sets parameters on a PreparedStatement based on the provided map.
     *
     * @param stmt       The PreparedStatement to set parameters for.
     * @param parameters The map of parameters to set.
     * @throws SQLException If a database access error occurs.
     */
    private static void setParameters(PreparedStatement stmt, Map<Integer, SqlParameter> parameters) throws SQLException {
        for (Map.Entry<Integer, SqlParameter> entry : parameters.entrySet()) {
            int index = entry.getKey();
            SqlParameter parameter = entry.getValue();

            switch (parameter.getType()) {
                case Types.INTEGER:
                    stmt.setInt(index, (Integer) parameter.getValue());
                    break;
                case Types.VARCHAR:
                    stmt.setString(index, (String) parameter.getValue());
                    break;
                case Types.DOUBLE:
                    stmt.setDouble(index, (Double) parameter.getValue());
                    break;
                case Types.DATE:
                    stmt.setDate(index, (Date) parameter.getValue());
                    break;
                case Types.BOOLEAN:
                    stmt.setBoolean(index, (Boolean) parameter.getValue());
                    break;
                default:
                    stmt.setObject(index, parameter.getValue());
                    break;
            }
        }
    }
}
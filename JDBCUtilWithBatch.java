import java.sql.*;
import java.util.List;
import java.util.Map;

public class JDBCUtil {

    /**
     * Executes a batch of SQL statements with parameters for different tables and controls the batch size.
     *
     * @param conn        The database connection.
     * @param sql         The SQL statement to execute in batch.
     * @param batchParams A list of parameter maps for each batch.
     * @param batchSize   The number of statements to include in each batch execution.
     * @return The total update counts for all executed batches.
     * @throws SQLException If a database access error occurs.
     */
    public static int[] executeInBatch(Connection conn, String sql, List<Map<Integer, SqlParameter>> batchParams, int batchSize) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int[] totalUpdateCounts = new int[0];
            int counter = 0;

            for (Map<Integer, SqlParameter> parameters : batchParams) {
                setParameters(stmt, parameters);
                stmt.addBatch();
                counter++;

                if (counter % batchSize == 0) {
                    totalUpdateCounts = mergeArrays(totalUpdateCounts, stmt.executeBatch());
                }
            }

            // Execute remaining batches
            if (counter % batchSize != 0) {
                totalUpdateCounts = mergeArrays(totalUpdateCounts, stmt.executeBatch());
            }

            return totalUpdateCounts;
        }
    }

    /**
     * Merges two integer arrays.
     *
     * @param first  The first array.
     * @param second The second array.
     * @return The merged array.
     */
    private static int[] mergeArrays(int[] first, int[] second) {
        int[] merged = new int[first.length + second.length];
        System.arraycopy(first, 0, merged, 0, first.length);
        System.arraycopy(second, 0, merged, first.length, second.length);
        return merged;
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
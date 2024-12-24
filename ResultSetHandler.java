@FunctionalInterface
public interface ResultSetHandler<T> {
    /**
     * Handles the processing of a ResultSet.
     *
     * @param resultSet The ResultSet to process
     * @return Processed result of type T
     * @throws SQLException if a database access error occurs
     */
    T handle(ResultSet resultSet) throws SQLException;
}
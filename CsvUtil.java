import org.apache.commons.csv.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class CSVUtil {

    /**
     * Converts a JDBC ResultSet to CSV format and returns it as a byte array.
     *
     * @param resultSet The ResultSet from the database query.
     * @return A byte array containing the CSV data.
     * @throws SQLException, IOException
     */
    public static byte[] resultSetToCSVBytes(ResultSet resultSet) throws SQLException, IOException {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(byteStream);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Writing header row
            List<String> headers = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                headers.add(metaData.getColumnName(i));
            }
            csvPrinter.printRecord(headers);

            // Writing data rows
            while (resultSet.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.add(resultSet.getString(i));
                }
                csvPrinter.printRecord(row);
            }

            csvPrinter.flush();
            return byteStream.toByteArray();
        }
    }

    /**
     * Stores CSV binary data in a BLOB column in the database.
     *
     * @param connection The JDBC Connection.
     * @param csvData The byte array containing CSV data.
     * @param tableName The table name where the BLOB is stored.
     * @param id The primary key for identification.
     * @throws SQLException
     */
    public static void storeCSVInBlob(Connection connection, byte[] csvData, String tableName, int id) throws SQLException {
        String sql = "UPDATE " + tableName + " SET csv_blob = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setBytes(1, csvData);
            preparedStatement.setInt(2, id);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Reads CSV binary data from a BLOB column and inserts it into a target table.
     *
     * @param connection The JDBC Connection.
     * @param tableName The table where the BLOB is stored.
     * @param id The primary key to locate the BLOB.
     * @param targetTable The database table where CSV data will be inserted.
     * @throws SQLException, IOException
     */
    public static void readCSVFromBlobAndInsertToDB(Connection connection, String tableName, int id, String targetTable) throws SQLException, IOException {
        String sql = "SELECT csv_blob FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                byte[] csvData = resultSet.getBytes("csv_blob");

                try (InputStream byteStream = new ByteArrayInputStream(csvData);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(byteStream));
                     CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                    List<String> columns = csvParser.getHeaderNames();
                    String insertSQL = buildInsertSQL(targetTable, columns);

                    try (PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {
                        for (CSVRecord record : csvParser) {
                            for (int i = 0; i < columns.size(); i++) {
                                insertStatement.setString(i + 1, record.get(columns.get(i)));
                            }
                            insertStatement.addBatch();
                        }
                        insertStatement.executeBatch();
                    }
                }
            }
        }
    }

    /**
     * Builds an SQL INSERT query dynamically based on column names.
     *
     * @param tableName The target table name.
     * @param columns The list of column names.
     * @return A dynamically constructed SQL INSERT query.
     */
    private static String buildInsertSQL(String tableName, List<String> columns) {
        String columnNames = String.join(", ", columns);
        String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));
        return "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + placeholders + ")";
    }
}
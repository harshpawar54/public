import org.apache.commons.csv.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class CSVUtil {

    /**
     * Writes the given JDBC ResultSet to a CSV file.
     * 
     * @param resultSet The ResultSet from the database query
     * @param filePath The file path to write the CSV data
     * @throws SQLException, IOException
     */
    public static void writeResultSetToCSV(ResultSet resultSet, String filePath) throws SQLException, IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
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
        }
    }

    /**
     * Reads a CSV file and inserts data into a database table.
     * 
     * @param filePath The CSV file path
     * @param connection The JDBC Connection object
     * @param tableName The target database table name
     * @throws SQLException, IOException
     */
    public static void readCSVAndInsertToDB(String filePath, Connection connection, String tableName) throws SQLException, IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            List<String> columns = csvParser.getHeaderNames();
            String insertSQL = buildInsertSQL(tableName, columns);

            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                for (CSVRecord record : csvParser) {
                    for (int i = 0; i < columns.size(); i++) {
                        preparedStatement.setString(i + 1, record.get(columns.get(i)));
                    }
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        }
    }

    /**
     * Builds an SQL INSERT query dynamically based on the column names.
     * 
     * @param tableName The table name
     * @param columns The list of column names
     * @return A dynamically constructed SQL INSERT query
     */
    private static String buildInsertSQL(String tableName, List<String> columns) {
        String columnNames = String.join(", ", columns);
        String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));
        return "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + placeholders + ")";
    }
}
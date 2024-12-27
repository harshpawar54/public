import java.sql.Connection;
import java.sql.Types;
import java.util.List;
import java.util.Map;

public class InsertWithGeneratedKeysExample {
    public static void main(String[] args) {
        String insertSql = "INSERT INTO users (name, email) VALUES (?, ?)";
        Map<Integer, SqlParameter> parameters = Map.of(
            1, new SqlParameter(Types.VARCHAR, "John Doe"),
            2, new SqlParameter(Types.VARCHAR, "john.doe@example.com")
        );

        try (Connection conn = JDBCUtil.getConnection()) {
            List<Object> generatedKeys = JDBCUtil.executeInsertWithGeneratedKeys(conn, insertSql, parameters);
            if (!generatedKeys.isEmpty()) {
                System.out.println("Generated Key: " + generatedKeys.get(0));
            } else {
                System.out.println("No keys generated.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
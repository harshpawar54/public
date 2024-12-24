import java.sql.Connection;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String sql = "SELECT id, name, email FROM users WHERE active = ?";
        Map<Integer, SqlParameter> parameters = new HashMap<>();
        parameters.put(1, new SqlParameter(Types.BOOLEAN, true)); // Parameter index: 1

        try (Connection conn = JDBCUtil.getConnection()) {
            // Using ResultSetHandler to map each row to a User object
            List<User> users = JDBCUtil.executeQuery(conn, sql, parameters, resultSet -> {
                List<User> result = new ArrayList<>();
                while (resultSet.next()) {
                    User user = new User(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email")
                    );
                    result.add(user);
                }
                return result;
            });

            // Output the processed result
            users.forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// User class
class User {
    private int id;
    private String name;
    private String email;

    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}